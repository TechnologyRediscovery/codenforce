-- schemaType: FUNCTION
-- db1: {cogdb localhost 5432 changeme changeme * sslmode=disable}
-- db2: {cog_db7 localhost 5432 changeme changeme * sslmode=disable}
-- Run the following SQL against db2:
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.__st_countagg_transfn(agg agg_count, rast raster, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, sample_percent double precision DEFAULT 1)
 RETURNS agg_count
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		_count bigint;
		rtn_agg agg_count;
	BEGIN

		-- only process parameter args once
		IF agg IS NULL THEN
			rtn_agg.count := 0;

			IF nband < 1 THEN
				RAISE EXCEPTION 'Band index must be greater than zero (1-based)';
			ELSE
				rtn_agg.nband := nband;
			END IF;

			IF exclude_nodata_value IS FALSE THEN
				rtn_agg.exclude_nodata_value := FALSE;
			ELSE
				rtn_agg.exclude_nodata_value := TRUE;
			END IF;

			IF sample_percent < 0. OR sample_percent > 1. THEN
				RAISE EXCEPTION 'Sample percent must be between zero and one';
			ELSE
				rtn_agg.sample_percent := sample_percent;
			END IF;
		ELSE
			rtn_agg := agg;
		END IF;

		IF rast IS NOT NULL THEN
			IF rtn_agg.exclude_nodata_value IS FALSE THEN
				SELECT width * height INTO _count FROM public.ST_Metadata(rast);
			ELSE
				SELECT count INTO _count FROM public._ST_summarystats(
					rast,
				 	rtn_agg.nband, rtn_agg.exclude_nodata_value,
					rtn_agg.sample_percent
				);
			END IF;
		END IF;

		rtn_agg.count := rtn_agg.count + _count;
		RETURN rtn_agg;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._add_overview_constraint(ovschema name, ovtable name, ovcolumn name, refschema name, reftable name, refcolumn name, factor integer)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		fqtn text;
		cn name;
		sql text;
	BEGIN
		fqtn := '';
		IF length($1) > 0 THEN
			fqtn := quote_ident($1) || '.';
		END IF;
		fqtn := fqtn || quote_ident($2);

		cn := 'enforce_overview_' || $3;

		sql := 'ALTER TABLE ' || fqtn
			|| ' ADD CONSTRAINT ' || quote_ident(cn)
			|| ' CHECK ( public._overview_constraint(' || quote_ident($3)
			|| ',' || $7
			|| ',' || quote_literal($4)
			|| ',' || quote_literal($5)
			|| ',' || quote_literal($6)
			|| '))';

		RETURN  public._add_raster_constraint(cn, sql);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._add_raster_constraint(cn name, sql text)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	BEGIN
		BEGIN
			EXECUTE sql;
		EXCEPTION
			WHEN duplicate_object THEN
				RAISE NOTICE 'The constraint "%" already exists.  To replace the existing constraint, delete the constraint and call ApplyRasterConstraints again', cn;
			WHEN OTHERS THEN
				RAISE NOTICE 'Unable to add constraint: %', cn;
				RAISE NOTICE 'SQL used for failed constraint: %', sql;
				RAISE NOTICE 'Returned error message: % (%)', SQLERRM, SQLSTATE;
				RETURN FALSE;
		END;

		RETURN TRUE;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._add_raster_constraint_alignment(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		fqtn text;
		cn name;
		sql text;
		attr text;
	BEGIN
		fqtn := '';
		IF length($1) > 0 THEN
			fqtn := quote_ident($1) || '.';
		END IF;
		fqtn := fqtn || quote_ident($2);

		cn := 'enforce_same_alignment_' || $3;

		sql := 'SELECT public.st_makeemptyraster(1, 1, upperleftx, upperlefty, scalex, scaley, skewx, skewy, srid) FROM public.st_metadata((SELECT '
			|| quote_ident($3)
			|| ' FROM '
			|| fqtn
			|| ' WHERE '
			|| quote_ident($3)
			|| ' IS NOT NULL LIMIT 1))';
		BEGIN
			EXECUTE sql INTO attr;
		EXCEPTION WHEN OTHERS THEN
			RAISE NOTICE 'Unable to get the alignment of a sample raster: % (%)',
        SQLERRM, SQLSTATE;
			RETURN FALSE;
		END;

		sql := 'ALTER TABLE ' || fqtn ||
			' ADD CONSTRAINT ' || quote_ident(cn) ||
			' CHECK (public.st_samealignment(' || quote_ident($3) || ', ''' || attr || '''::raster))';
		RETURN  public._add_raster_constraint(cn, sql);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._add_raster_constraint_blocksize(rastschema name, rasttable name, rastcolumn name, axis text)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		fqtn text;
		cn name;
		sql text;
		attrset integer[];
		attr integer;
	BEGIN
		IF lower($4) != 'width' AND lower($4) != 'height' THEN
			RAISE EXCEPTION 'axis must be either "width" or "height"';
			RETURN FALSE;
		END IF;

		fqtn := '';
		IF length($1) > 0 THEN
			fqtn := quote_ident($1) || '.';
		END IF;
		fqtn := fqtn || quote_ident($2);

		cn := 'enforce_' || $4 || '_' || $3;

		sql := 'SELECT st_' || $4 || '('
			|| quote_ident($3)
			|| ') FROM ' || fqtn
			|| ' GROUP BY 1 ORDER BY count(*) DESC';
		BEGIN
			attrset := ARRAY[]::integer[];
			FOR attr IN EXECUTE sql LOOP
				attrset := attrset || attr;
			END LOOP;
		EXCEPTION WHEN OTHERS THEN
			RAISE NOTICE 'Unable to get the % of a sample raster: % (%)',
        $4, SQLERRM, SQLSTATE;
			RETURN FALSE;
		END;

		sql := 'ALTER TABLE ' || fqtn
			|| ' ADD CONSTRAINT ' || quote_ident(cn)
			|| ' CHECK (st_' || $4 || '('
			|| quote_ident($3)
			|| ') IN (' || array_to_string(attrset, ',') || '))';
		RETURN  public._add_raster_constraint(cn, sql);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._add_raster_constraint_coverage_tile(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		fqtn text;
		cn name;
		sql text;

		_scalex double precision;
		_scaley double precision;
		_skewx double precision;
		_skewy double precision;
		_tilewidth integer;
		_tileheight integer;
		_alignment boolean;

		_covextent public.geometry;
		_covrast public.raster;
	BEGIN
		fqtn := '';
		IF length($1) > 0 THEN
			fqtn := quote_ident($1) || '.';
		END IF;
		fqtn := fqtn || quote_ident($2);

		cn := 'enforce_coverage_tile_' || quote_ident($3);

		-- metadata
		BEGIN
			sql := 'WITH foo AS (SELECT public.ST_Metadata(' || quote_ident($3) || ') AS meta, public.ST_ConvexHull(' || quote_ident($3) || ') AS hull FROM ' || fqtn || ') SELECT max((meta).scalex), max((meta).scaley), max((meta).skewx), max((meta).skewy), max((meta).width), max((meta).height), public.ST_Union(hull) FROM foo';
			EXECUTE sql INTO _scalex, _scaley, _skewx, _skewy, _tilewidth, _tileheight, _covextent;
		EXCEPTION WHEN OTHERS THEN
			RAISE DEBUG 'Unable to get coverage metadata for %.%: % (%)',
        fqtn, quote_ident($3), SQLERRM, SQLSTATE;
      -- TODO: Why not return false here ?
		END;

		-- rasterize extent
		BEGIN
			_covrast := public.ST_AsRaster(_covextent, _scalex, _scaley, '8BUI', 1, 0, NULL, NULL, _skewx, _skewy);
			IF _covrast IS NULL THEN
				RAISE NOTICE 'Unable to create coverage raster. Cannot add coverage tile constraint: % (%)',
          SQLERRM, SQLSTATE;
				RETURN FALSE;
			END IF;

			-- remove band
			_covrast := public.ST_MakeEmptyRaster(_covrast);
		EXCEPTION WHEN OTHERS THEN
			RAISE NOTICE 'Unable to create coverage raster. Cannot add coverage tile constraint: % (%)',
        SQLERRM, SQLSTATE;
			RETURN FALSE;
		END;

		sql := 'ALTER TABLE ' || fqtn ||
			' ADD CONSTRAINT ' || quote_ident(cn) ||
			' CHECK (st_iscoveragetile(' || quote_ident($3) || ', ''' || _covrast || '''::raster, ' || _tilewidth || ', ' || _tileheight || '))';
		RETURN  public._add_raster_constraint(cn, sql);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._add_raster_constraint_extent(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT COST 9000
AS $function$
	DECLARE
		fqtn text;
		cn name;
		sql text;
		attr text; srid integer;
	BEGIN
		fqtn := '';
		IF length(rastschema) > 0 THEN
			fqtn := quote_ident(rastschema) || '.';
		END IF;
		fqtn := fqtn || quote_ident(rasttable);

		sql := 'SELECT public.ST_SRID('
			|| quote_ident(rastcolumn)
			|| ') FROM '
			|| fqtn
			|| ' WHERE '
			|| quote_ident(rastcolumn)
			|| ' IS NOT NULL LIMIT 1;';
                EXECUTE sql INTO srid;

    IF srid IS NULL THEN
      RETURN false;
    END IF;

		cn := 'enforce_max_extent_' || rastcolumn;

		sql := 'SELECT public.st_ashexewkb( public.st_setsrid( public.st_extent( public.st_envelope('
			|| quote_ident(rastcolumn)
			|| ')), ' || srid || ')) FROM '
			|| fqtn;
		EXECUTE sql INTO attr;

		-- NOTE: I put NOT VALID to prevent the costly step of validating the constraint
		sql := 'ALTER TABLE ' || fqtn
			|| ' ADD CONSTRAINT ' || quote_ident(cn)
			|| ' CHECK ( public.st_envelope('
			|| quote_ident(rastcolumn)
			|| ') @ ''' || attr || '''::geometry) NOT VALID';
		RETURN  public._add_raster_constraint(cn, sql);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._add_raster_constraint_nodata_values(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		fqtn text;
		cn name;
		sql text;
		attr numeric[];
		max int;
	BEGIN
		fqtn := '';
		IF length($1) > 0 THEN
			fqtn := quote_ident($1) || '.';
		END IF;
		fqtn := fqtn || quote_ident($2);

		cn := 'enforce_nodata_values_' || $3;

		sql := 'SELECT public._raster_constraint_nodata_values(' || quote_ident($3)
			|| ') FROM ' || fqtn
			|| ' WHERE '
			|| quote_ident($3)
			|| ' IS NOT NULL LIMIT 1;';
		BEGIN
			EXECUTE sql INTO attr;
		EXCEPTION WHEN OTHERS THEN
			RAISE NOTICE 'Unable to get the nodata values of a sample raster: % (%)',
        SQLERRM, SQLSTATE;
			RETURN FALSE;
		END;
		max := array_length(attr, 1);
		IF max < 1 OR max IS NULL THEN
			RAISE NOTICE 'Unable to get the nodata values of a sample raster (max < 1 or null)';
			RETURN FALSE;
		END IF;

		sql := 'ALTER TABLE ' || fqtn
			|| ' ADD CONSTRAINT ' || quote_ident(cn)
			|| ' CHECK (_raster_constraint_nodata_values(' || quote_ident($3)
			|| ')::numeric[] = ''{';
		FOR x in 1..max LOOP
			IF attr[x] IS NULL THEN
				sql := sql || 'NULL';
			ELSE
				sql := sql || attr[x];
			END IF;
			IF x < max THEN
				sql := sql || ',';
			END IF;
		END LOOP;
		sql := sql || '}''::numeric[])';

		RETURN  public._add_raster_constraint(cn, sql);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._add_raster_constraint_num_bands(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		fqtn text;
		cn name;
		sql text;
		attr int;
	BEGIN
		fqtn := '';
		IF length($1) > 0 THEN
			fqtn := quote_ident($1) || '.';
		END IF;
		fqtn := fqtn || quote_ident($2);

		cn := 'enforce_num_bands_' || $3;

		sql := 'SELECT public.st_numbands(' || quote_ident($3)
			|| ') FROM '
			|| fqtn
			|| ' WHERE '
			|| quote_ident($3)
			|| ' IS NOT NULL LIMIT 1;';
		BEGIN
			EXECUTE sql INTO attr;
		EXCEPTION WHEN OTHERS THEN
			RAISE NOTICE 'Unable to get the number of bands of a sample raster: % (%)',
        SQLERRM, SQLSTATE;
			RETURN FALSE;
		END;

		sql := 'ALTER TABLE ' || fqtn
			|| ' ADD CONSTRAINT ' || quote_ident(cn)
			|| ' CHECK (public.st_numbands(' || quote_ident($3)
			|| ') = ' || attr
			|| ')';
		RETURN  public._add_raster_constraint(cn, sql);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._add_raster_constraint_out_db(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		fqtn text;
		cn name;
		sql text;
		attr boolean[];
		max int;
	BEGIN
		fqtn := '';
		IF length($1) > 0 THEN
			fqtn := quote_ident($1) || '.';
		END IF;
		fqtn := fqtn || quote_ident($2);

		cn := 'enforce_out_db_' || $3;

		sql := 'SELECT public._raster_constraint_out_db(' || quote_ident($3)
			|| ') FROM ' || fqtn
			|| ' WHERE '
			|| quote_ident($3)
			|| ' IS NOT NULL LIMIT 1;';
		BEGIN
			EXECUTE sql INTO attr;
		EXCEPTION WHEN OTHERS THEN
			RAISE NOTICE 'Unable to get the out-of-database bands of a sample raster: % (%)',
        SQLERRM, SQLSTATE;
			RETURN FALSE;
		END;
		max := array_length(attr, 1);
		IF max < 1 OR max IS NULL THEN
			RAISE NOTICE 'Unable to get the out-of-database bands of a sample raster (max < 1 or null)';
			RETURN FALSE;
		END IF;

		sql := 'ALTER TABLE ' || fqtn
			|| ' ADD CONSTRAINT ' || quote_ident(cn)
			|| ' CHECK ( public._raster_constraint_out_db(' || quote_ident($3)
			|| ') = ''{';
		FOR x in 1..max LOOP
			IF attr[x] IS FALSE THEN
				sql := sql || 'FALSE';
			ELSE
				sql := sql || 'TRUE';
			END IF;
			IF x < max THEN
				sql := sql || ',';
			END IF;
		END LOOP;
		sql := sql || '}''::boolean[])';

		RETURN  public._add_raster_constraint(cn, sql);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._add_raster_constraint_pixel_types(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		fqtn text;
		cn name;
		sql text;
		attr text[];
		max int;
	BEGIN
		fqtn := '';
		IF length($1) > 0 THEN
			fqtn := quote_ident($1) || '.';
		END IF;
		fqtn := fqtn || quote_ident($2);

		cn := 'enforce_pixel_types_' || $3;

		sql := 'SELECT public._raster_constraint_pixel_types(' || quote_ident($3)
			|| ') FROM ' || fqtn
			|| ' WHERE '
			|| quote_ident($3)
			|| ' IS NOT NULL LIMIT 1;';
		BEGIN
			EXECUTE sql INTO attr;
		EXCEPTION WHEN OTHERS THEN
			RAISE NOTICE 'Unable to get the pixel types of a sample raster: % (%)',
        SQLERRM, SQLSTATE;
			RETURN FALSE;
		END;
		max := array_length(attr, 1);
		IF max < 1 OR max IS NULL THEN
			RAISE NOTICE 'Unable to get the pixel types of a sample raster (max < 1 or null)';
			RETURN FALSE;
		END IF;

		sql := 'ALTER TABLE ' || fqtn
			|| ' ADD CONSTRAINT ' || quote_ident(cn)
			|| ' CHECK (public._raster_constraint_pixel_types(' || quote_ident($3)
			|| ') = ''{';
		FOR x in 1..max LOOP
			sql := sql || '"' || attr[x] || '"';
			IF x < max THEN
				sql := sql || ',';
			END IF;
		END LOOP;
		sql := sql || '}''::text[])';

		RETURN  public._add_raster_constraint(cn, sql);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._add_raster_constraint_scale(rastschema name, rasttable name, rastcolumn name, axis character)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		fqtn text;
		cn name;
		sql text;
		attr double precision;
	BEGIN
		IF lower($4) != 'x' AND lower($4) != 'y' THEN
			RAISE EXCEPTION 'axis must be either "x" or "y"';
			RETURN FALSE;
		END IF;

		fqtn := '';
		IF length($1) > 0 THEN
			fqtn := quote_ident($1) || '.';
		END IF;
		fqtn := fqtn || quote_ident($2);

		cn := 'enforce_scale' || $4 || '_' || $3;

		sql := 'SELECT public.st_scale' || $4 || '('
			|| quote_ident($3)
			|| ') FROM '
			|| fqtn
			|| ' WHERE '
			|| quote_ident($3)
			|| ' IS NOT NULL LIMIT 1;';
		BEGIN
			EXECUTE sql INTO attr;
		EXCEPTION WHEN OTHERS THEN
			RAISE NOTICE 'Unable to get the %-scale of a sample raster: % (%)',
        upper($4), SQLERRM, SQLSTATE;
			RETURN FALSE;
		END;

		sql := 'ALTER TABLE ' || fqtn
			|| ' ADD CONSTRAINT ' || quote_ident(cn)
			|| ' CHECK (round(public.st_scale' || $4 || '('
			|| quote_ident($3)
			|| ')::numeric, 10) = round(' || text(attr) || '::numeric, 10))';
		RETURN  public._add_raster_constraint(cn, sql);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._add_raster_constraint_spatially_unique(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		fqtn text;
		cn name;
		sql text;
		attr text;
		meta record;
	BEGIN
		fqtn := '';
		IF length($1) > 0 THEN
			fqtn := quote_ident($1) || '.';
		END IF;
		fqtn := fqtn || quote_ident($2);

		cn := 'enforce_spatially_unique_' || quote_ident($2) || '_'|| $3;

		sql := 'ALTER TABLE ' || fqtn ||
			' ADD CONSTRAINT ' || quote_ident(cn) ||
			' EXCLUDE ((' || quote_ident($3) || '::public.geometry) WITH =)';
		RETURN  public._add_raster_constraint(cn, sql);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._add_raster_constraint_srid(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		fqtn text;
		cn name;
		sql text;
		attr int;
	BEGIN
		fqtn := '';
		IF length($1) > 0 THEN
			fqtn := quote_ident($1) || '.';
		END IF;
		fqtn := fqtn || quote_ident($2);

		cn := 'enforce_srid_' || $3;

		sql := 'SELECT public.st_srid('
			|| quote_ident($3)
			|| ') FROM ' || fqtn
			|| ' WHERE '
			|| quote_ident($3)
			|| ' IS NOT NULL LIMIT 1;';
		BEGIN
			EXECUTE sql INTO attr;
		EXCEPTION WHEN OTHERS THEN
			RAISE NOTICE 'Unable to get the SRID of a sample raster: % (%)',
        SQLERRM, SQLSTATE;
			RETURN FALSE;
		END;

		sql := 'ALTER TABLE ' || fqtn
			|| ' ADD CONSTRAINT ' || quote_ident(cn)
			|| ' CHECK (public.st_srid('
			|| quote_ident($3)
			|| ') = ' || attr || ')';

		RETURN  public._add_raster_constraint(cn, sql);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._drop_overview_constraint(ovschema name, ovtable name, ovcolumn name)
 RETURNS boolean
 LANGUAGE sql
 STRICT
AS $function$ SELECT  public._drop_raster_constraint($1, $2, 'enforce_overview_' || $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._drop_raster_constraint(rastschema name, rasttable name, cn name)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		fqtn text;
	BEGIN
		fqtn := '';
		IF length($1) > 0 THEN
			fqtn := quote_ident($1) || '.';
		END IF;
		fqtn := fqtn || quote_ident($2);

		BEGIN
			EXECUTE 'ALTER TABLE '
				|| fqtn
				|| ' DROP CONSTRAINT '
				|| quote_ident(cn);
			RETURN TRUE;
		EXCEPTION
			WHEN undefined_object THEN
				RAISE NOTICE 'The constraint "%" does not exist.  Skipping', cn;
			WHEN OTHERS THEN
				RAISE NOTICE 'Unable to drop constraint "%": % (%)',
          cn, SQLERRM, SQLSTATE;
				RETURN FALSE;
		END;

		RETURN TRUE;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._drop_raster_constraint_alignment(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE sql
 STRICT
AS $function$ SELECT  public._drop_raster_constraint($1, $2, 'enforce_same_alignment_' || $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._drop_raster_constraint_blocksize(rastschema name, rasttable name, rastcolumn name, axis text)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	BEGIN
		IF lower($4) != 'width' AND lower($4) != 'height' THEN
			RAISE EXCEPTION 'axis must be either "width" or "height"';
			RETURN FALSE;
		END IF;

		RETURN  public._drop_raster_constraint($1, $2, 'enforce_' || $4 || '_' || $3);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._drop_raster_constraint_coverage_tile(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE sql
 STRICT
AS $function$ SELECT  public._drop_raster_constraint($1, $2, 'enforce_coverage_tile_' || $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._drop_raster_constraint_extent(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE sql
 STRICT
AS $function$ SELECT  public._drop_raster_constraint($1, $2, 'enforce_max_extent_' || $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._drop_raster_constraint_nodata_values(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE sql
 STRICT
AS $function$ SELECT  public._drop_raster_constraint($1, $2, 'enforce_nodata_values_' || $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._drop_raster_constraint_num_bands(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE sql
 STRICT
AS $function$ SELECT  public._drop_raster_constraint($1, $2, 'enforce_num_bands_' || $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._drop_raster_constraint_out_db(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE sql
 STRICT
AS $function$ SELECT  public._drop_raster_constraint($1, $2, 'enforce_out_db_' || $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._drop_raster_constraint_pixel_types(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE sql
 STRICT
AS $function$ SELECT  public._drop_raster_constraint($1, $2, 'enforce_pixel_types_' || $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._drop_raster_constraint_regular_blocking(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE sql
 STRICT
AS $function$ SELECT public._drop_raster_constraint($1, $2, 'enforce_regular_blocking_' || $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._drop_raster_constraint_scale(rastschema name, rasttable name, rastcolumn name, axis character)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	BEGIN
		IF lower($4) != 'x' AND lower($4) != 'y' THEN
			RAISE EXCEPTION 'axis must be either "x" or "y"';
			RETURN FALSE;
		END IF;

		RETURN  public._drop_raster_constraint($1, $2, 'enforce_scale' || $4 || '_' || $3);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._drop_raster_constraint_spatially_unique(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		cn text;
	BEGIN
		SELECT
			s.conname INTO cn
		FROM pg_class c, pg_namespace n, pg_attribute a
		, (SELECT connamespace, conname, conrelid, conkey, conindid, contype, conexclop, pg_get_constraintdef(oid) As consrc
			FROM pg_constraint) AS s
		, pg_index idx, pg_operator op
		WHERE n.nspname = $1
			AND c.relname = $2
			AND a.attname = $3
			AND a.attrelid = c.oid
			AND s.connamespace = n.oid
			AND s.conrelid = c.oid
			AND s.contype = 'x'
			AND 0::smallint = ANY (s.conkey)
			AND idx.indexrelid = s.conindid
			AND pg_get_indexdef(idx.indexrelid, 1, true) LIKE '(' || quote_ident($3) || '::%geometry)'
			AND s.conexclop[1] = op.oid
			AND op.oprname = '=';

		RETURN  public._drop_raster_constraint($1, $2, cn);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._drop_raster_constraint_srid(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE sql
 STRICT
AS $function$ SELECT  public._drop_raster_constraint($1, $2, 'enforce_srid_' || $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._overview_constraint(ov raster, factor integer, refschema name, reftable name, refcolumn name)
 RETURNS boolean
 LANGUAGE sql
 STABLE
AS $function$ SELECT COALESCE((SELECT TRUE FROM public.raster_columns WHERE r_table_catalog = current_database() AND r_table_schema = $3 AND r_table_name = $4 AND r_raster_column = $5), FALSE) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._overview_constraint_info(ovschema name, ovtable name, ovcolumn name, OUT refschema name, OUT reftable name, OUT refcolumn name, OUT factor integer)
 RETURNS record
 LANGUAGE sql
 STABLE STRICT
AS $function$
	SELECT
		split_part(split_part(s.consrc, '''::name', 1), '''', 2)::name,
		split_part(split_part(s.consrc, '''::name', 2), '''', 2)::name,
		split_part(split_part(s.consrc, '''::name', 3), '''', 2)::name,
		trim(both from split_part(s.consrc, ',', 2))::integer
	FROM pg_class c, pg_namespace n, pg_attribute a
		, (SELECT connamespace, conrelid, conkey, pg_get_constraintdef(oid) As consrc
			FROM pg_constraint) AS s
	WHERE n.nspname = $1
		AND c.relname = $2
		AND a.attname = $3
		AND a.attrelid = c.oid
		AND s.connamespace = n.oid
		AND s.conrelid = c.oid
		AND a.attnum = ANY (s.conkey)
		AND s.consrc LIKE '%_overview_constraint(%'
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._postgis_deprecate(oldname text, newname text, version text)
 RETURNS void
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
DECLARE
  curver_text text;
BEGIN
  --
  -- Raises a NOTICE if it was deprecated in this version,
  -- a WARNING if in a previous version (only up to minor version checked)
  --
    curver_text := '2.5.5';
    IF split_part(curver_text,'.',1)::int > split_part(version,'.',1)::int OR
       ( split_part(curver_text,'.',1) = split_part(version,'.',1) AND
         split_part(curver_text,'.',2) != split_part(version,'.',2) )
    THEN
      RAISE WARNING '% signature was deprecated in %. Please use %', oldname, version, newname;
    ELSE
      RAISE DEBUG '% signature was deprecated in %. Please use %', oldname, version, newname;
    END IF;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._postgis_index_extent(tbl regclass, col text)
 RETURNS box2d
 LANGUAGE c
 STABLE STRICT
AS '$libdir/postgis-2.5', $function$_postgis_gserialized_index_extent$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._postgis_join_selectivity(regclass, text, regclass, text, text DEFAULT '2'::text)
 RETURNS double precision
 LANGUAGE c
 STRICT
AS '$libdir/postgis-2.5', $function$_postgis_gserialized_joinsel$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._postgis_pgsql_version()
 RETURNS text
 LANGUAGE sql
 STABLE
AS $function$
	SELECT CASE WHEN split_part(s,'.',1)::integer > 9 THEN split_part(s,'.',1) || '0' ELSE split_part(s,'.', 1) || split_part(s,'.', 2) END AS v
	FROM substring(version(), 'PostgreSQL ([0-9\.]+)') AS s;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._postgis_scripts_pgsql_version()
 RETURNS text
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT '95'::text AS version$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._postgis_selectivity(tbl regclass, att_name text, geom geometry, mode text DEFAULT '2'::text)
 RETURNS double precision
 LANGUAGE c
 STRICT
AS '$libdir/postgis-2.5', $function$_postgis_gserialized_sel$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._postgis_stats(tbl regclass, att_name text, text DEFAULT '2'::text)
 RETURNS text
 LANGUAGE c
 STRICT
AS '$libdir/postgis-2.5', $function$_postgis_gserialized_stats$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._raster_constraint_info_alignment(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE sql
 STABLE STRICT
AS $function$
	SELECT
		TRUE
	FROM pg_class c, pg_namespace n, pg_attribute a
		, (SELECT connamespace, conrelid, conkey, pg_get_constraintdef(oid) As consrc
			FROM pg_constraint) AS s
	WHERE n.nspname = $1
		AND c.relname = $2
		AND a.attname = $3
		AND a.attrelid = c.oid
		AND s.connamespace = n.oid
		AND s.conrelid = c.oid
		AND a.attnum = ANY (s.conkey)
		AND s.consrc LIKE '%st_samealignment(%';
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._raster_constraint_info_blocksize(rastschema name, rasttable name, rastcolumn name, axis text)
 RETURNS integer
 LANGUAGE sql
 STABLE STRICT
AS $function$
	SELECT
		CASE
			WHEN strpos(s.consrc, 'ANY (ARRAY[') > 0 THEN
				split_part((substring(s.consrc FROM E'ARRAY\\[(.*?){1}\\]')), ',', 1)::integer
			ELSE
				regexp_replace(
					split_part(s.consrc, '= ', 2),
					'[\(\)]', '', 'g'
				)::integer
			END
	FROM pg_class c, pg_namespace n, pg_attribute a
		, (SELECT connamespace, conrelid, conkey, pg_get_constraintdef(oid) As consrc
			FROM pg_constraint) AS s
	WHERE n.nspname = $1
		AND c.relname = $2
		AND a.attname = $3
		AND a.attrelid = c.oid
		AND s.connamespace = n.oid
		AND s.conrelid = c.oid
		AND a.attnum = ANY (s.conkey)
		AND s.consrc LIKE '%st_' || $4 || '(%= %';
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._raster_constraint_info_coverage_tile(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE sql
 STABLE STRICT
AS $function$
	SELECT
		TRUE
	FROM pg_class c, pg_namespace n, pg_attribute a
			, (SELECT connamespace, conrelid, conkey, pg_get_constraintdef(oid) As consrc
			FROM pg_constraint) AS s
	WHERE n.nspname = $1
		AND c.relname = $2
		AND a.attname = $3
		AND a.attrelid = c.oid
		AND s.connamespace = n.oid
		AND s.conrelid = c.oid
		AND a.attnum = ANY (s.conkey)
		AND s.consrc LIKE '%st_iscoveragetile(%';
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._raster_constraint_info_extent(rastschema name, rasttable name, rastcolumn name)
 RETURNS geometry
 LANGUAGE sql
 STABLE STRICT
AS $function$
	SELECT
		trim(both '''' from split_part(trim(split_part(s.consrc, ' @ ', 2)), '::', 1))::public.geometry
	FROM pg_class c, pg_namespace n, pg_attribute a
		, (SELECT connamespace, conrelid, conkey, pg_get_constraintdef(oid) As consrc
			FROM pg_constraint) AS s
	WHERE n.nspname = $1
		AND c.relname = $2
		AND a.attname = $3
		AND a.attrelid = c.oid
		AND s.connamespace = n.oid
		AND s.conrelid = c.oid
		AND a.attnum = ANY (s.conkey)
		AND s.consrc LIKE '%st_envelope(% @ %';
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._raster_constraint_info_index(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE sql
 STABLE STRICT
AS $function$
		SELECT
			TRUE
		FROM pg_catalog.pg_class c
		JOIN pg_catalog.pg_index i
			ON i.indexrelid = c.oid
		JOIN pg_catalog.pg_class c2
			ON i.indrelid = c2.oid
		JOIN pg_catalog.pg_namespace n
			ON n.oid = c.relnamespace
		JOIN pg_am am
			ON c.relam = am.oid
		JOIN pg_attribute att
			ON att.attrelid = c2.oid
				AND pg_catalog.format_type(att.atttypid, att.atttypmod) = 'raster'
		WHERE c.relkind IN ('i')
			AND n.nspname = $1
			AND c2.relname = $2
			AND att.attname = $3
			AND am.amname = 'gist'
			AND strpos(pg_catalog.pg_get_expr(i.indexprs, i.indrelid), att.attname) > 0;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._raster_constraint_info_nodata_values(rastschema name, rasttable name, rastcolumn name)
 RETURNS double precision[]
 LANGUAGE sql
 STABLE STRICT
AS $function$
	SELECT
		trim(both '''' from
			split_part(
				regexp_replace(
					split_part(s.consrc, ' = ', 2),
					'[\(\)]', '', 'g'
				),
				'::', 1
			)
		)::double precision[]
	FROM pg_class c, pg_namespace n, pg_attribute a
		, (SELECT connamespace, conrelid, conkey, pg_get_constraintdef(oid) As consrc
			FROM pg_constraint) AS s
	WHERE n.nspname = $1
		AND c.relname = $2
		AND a.attname = $3
		AND a.attrelid = c.oid
		AND s.connamespace = n.oid
		AND s.conrelid = c.oid
		AND a.attnum = ANY (s.conkey)
		AND s.consrc LIKE '%_raster_constraint_nodata_values(%';
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._raster_constraint_info_num_bands(rastschema name, rasttable name, rastcolumn name)
 RETURNS integer
 LANGUAGE sql
 STABLE STRICT
AS $function$
	SELECT
		regexp_replace(
			split_part(s.consrc, ' = ', 2),
			'[\(\)]', '', 'g'
		)::integer
	FROM pg_class c, pg_namespace n, pg_attribute a
		, (SELECT connamespace, conrelid, conkey, pg_get_constraintdef(oid) As consrc
			FROM pg_constraint) AS s
	WHERE n.nspname = $1
		AND c.relname = $2
		AND a.attname = $3
		AND a.attrelid = c.oid
		AND s.connamespace = n.oid
		AND s.conrelid = c.oid
		AND a.attnum = ANY (s.conkey)
		AND s.consrc LIKE '%st_numbands(%';
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._raster_constraint_info_out_db(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean[]
 LANGUAGE sql
 STABLE STRICT
AS $function$
	SELECT
		trim(
			both '''' from split_part(
				regexp_replace(
					split_part(s.consrc, ' = ', 2),
					'[\(\)]', '', 'g'
				),
				'::', 1
			)
		)::boolean[]
	FROM pg_class c, pg_namespace n, pg_attribute a
			, (SELECT connamespace, conrelid, conkey, pg_get_constraintdef(oid) As consrc
			FROM pg_constraint) AS s
	WHERE n.nspname = $1
		AND c.relname = $2
		AND a.attname = $3
		AND a.attrelid = c.oid
		AND s.connamespace = n.oid
		AND s.conrelid = c.oid
		AND a.attnum = ANY (s.conkey)
		AND s.consrc LIKE '%_raster_constraint_out_db(%';
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._raster_constraint_info_pixel_types(rastschema name, rasttable name, rastcolumn name)
 RETURNS text[]
 LANGUAGE sql
 STABLE STRICT
AS $function$
	SELECT
		trim(
			both '''' from split_part(
				regexp_replace(
					split_part(s.consrc, ' = ', 2),
					'[\(\)]', '', 'g'
				),
				'::', 1
			)
		)::text[]
	FROM pg_class c, pg_namespace n, pg_attribute a
		, (SELECT connamespace, conrelid, conkey, pg_get_constraintdef(oid) As consrc
			FROM pg_constraint) AS s
	WHERE n.nspname = $1
		AND c.relname = $2
		AND a.attname = $3
		AND a.attrelid = c.oid
		AND s.connamespace = n.oid
		AND s.conrelid = c.oid
		AND a.attnum = ANY (s.conkey)
		AND s.consrc LIKE '%_raster_constraint_pixel_types(%';
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._raster_constraint_info_regular_blocking(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE plpgsql
 STABLE STRICT
AS $function$
	DECLARE
		covtile boolean;
		spunique boolean;
	BEGIN
		-- check existance of constraints
		-- coverage tile constraint
		covtile := COALESCE( public._raster_constraint_info_coverage_tile($1, $2, $3), FALSE);

		-- spatially unique constraint
		spunique := COALESCE( public._raster_constraint_info_spatially_unique($1, $2, $3), FALSE);

		RETURN (covtile AND spunique);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._raster_constraint_info_scale(rastschema name, rasttable name, rastcolumn name, axis character)
 RETURNS double precision
 LANGUAGE sql
 STABLE STRICT
AS $function$
	WITH c AS (SELECT
		regexp_replace(
			replace(
				split_part(
					split_part(s.consrc, ' = ', 2),
					'::', 1
				),
				'round(', ''
			),
			'[ ''''\(\)]', '', 'g'
		)::text AS val
	FROM pg_class c, pg_namespace n, pg_attribute a
		, (SELECT connamespace, conrelid, conkey, pg_get_constraintdef(oid) As consrc
			FROM pg_constraint) AS s
	WHERE n.nspname = $1
		AND c.relname = $2
		AND a.attname = $3
		AND a.attrelid = c.oid
		AND s.connamespace = n.oid
		AND s.conrelid = c.oid
		AND a.attnum = ANY (s.conkey)
		AND s.consrc LIKE '%st_scale' || $4 || '(% = %')
-- if it is a comma separated list of two numbers then need to use round
   SELECT CASE WHEN split_part(c.val,',', 2) > ''
        THEN round( split_part(c.val, ',',1)::numeric, split_part(c.val,',',2)::integer )::float8
        ELSE c.val::float8 END
        FROM c;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._raster_constraint_info_spatially_unique(rastschema name, rasttable name, rastcolumn name)
 RETURNS boolean
 LANGUAGE sql
 STABLE STRICT
AS $function$
	SELECT
		TRUE
	FROM pg_class c, pg_namespace n, pg_attribute a
		, (SELECT connamespace, conrelid, conindid, conkey, contype, conexclop, pg_get_constraintdef(oid) As consrc
			FROM pg_constraint) AS s
		, pg_index idx, pg_operator op
	WHERE n.nspname = $1
		AND c.relname = $2
		AND a.attname = $3
		AND a.attrelid = c.oid
		AND s.connamespace = n.oid
		AND s.conrelid = c.oid
		AND s.contype = 'x'
		AND 0::smallint = ANY (s.conkey)
		AND idx.indexrelid = s.conindid
		AND pg_get_indexdef(idx.indexrelid, 1, true) LIKE '(' || quote_ident($3) || '::%geometry)'
		AND s.conexclop[1] = op.oid
		AND op.oprname = '=';
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._raster_constraint_info_srid(rastschema name, rasttable name, rastcolumn name)
 RETURNS integer
 LANGUAGE sql
 STABLE STRICT
AS $function$
	SELECT
		regexp_replace(
			split_part(s.consrc, ' = ', 2),
			'[\(\)]', '', 'g'
		)::integer
	FROM pg_class c, pg_namespace n, pg_attribute a
		, (SELECT connamespace, conrelid, conkey, pg_get_constraintdef(oid) As consrc
		    FROM pg_constraint) AS s
	WHERE n.nspname = $1
		AND c.relname = $2
		AND a.attname = $3
		AND a.attrelid = c.oid
		AND s.connamespace = n.oid
		AND s.conrelid = c.oid
		AND a.attnum = ANY (s.conkey)
		AND s.consrc LIKE '%st_srid(% = %';
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._raster_constraint_nodata_values(rast raster)
 RETURNS numeric[]
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT array_agg(round(nodatavalue::numeric, 10))::numeric[] FROM public.ST_BandMetaData($1, ARRAY[]::int[]); $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._raster_constraint_out_db(rast raster)
 RETURNS boolean[]
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT array_agg(isoutdb)::boolean[] FROM public.ST_BandMetaData($1, ARRAY[]::int[]); $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._raster_constraint_pixel_types(rast raster)
 RETURNS text[]
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT array_agg(pixeltype)::text[] FROM  public.ST_BandMetaData($1, ARRAY[]::int[]); $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_3ddfullywithin(geom1 geometry, geom2 geometry, double precision)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$LWGEOM_dfullywithin3d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_3ddwithin(geom1 geometry, geom2 geometry, double precision)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$LWGEOM_dwithin3d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_3dintersects(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$intersects3d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_asgeojson(integer, geometry, integer, integer)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_AsGeoJson($2::public.geometry, $3::int4, $4::int4); $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_asgeojson(integer, geography, integer, integer)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_as_geojson$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_asgml(integer, geometry, integer, integer, text, text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE COST 2500
AS '$libdir/postgis-2.5', $function$LWGEOM_asGML$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_asgml(integer, geography, integer, integer, text, text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$geography_as_gml$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_askml(integer, geography, integer, text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$geography_as_kml$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_askml(integer, geometry, integer, text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE COST 5000
AS '$libdir/postgis-2.5', $function$LWGEOM_asKML$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_aspect4ma(value double precision[], pos integer[], VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		x integer;
		y integer;
		z integer;

		_width double precision;
		_height double precision;
		_units text;

		dz_dx double precision;
		dz_dy double precision;
		aspect double precision;
		halfpi double precision;

		_value double precision[][][];
		ndims int;
	BEGIN
		ndims := array_ndims(value);
		-- add a third dimension if 2-dimension
		IF ndims = 2 THEN
			_value := public._ST_convertarray4ma(value);
		ELSEIF ndims != 3 THEN
			RAISE EXCEPTION 'First parameter of function must be a 3-dimension array';
		ELSE
			_value := value;
		END IF;

		IF (
			array_lower(_value, 2) != 1 OR array_upper(_value, 2) != 3 OR
			array_lower(_value, 3) != 1 OR array_upper(_value, 3) != 3
		) THEN
			RAISE EXCEPTION 'First parameter of function must be a 1x3x3 array with each of the lower bounds starting from 1';
		END IF;

		IF array_length(userargs, 1) < 3 THEN
			RAISE EXCEPTION 'At least three elements must be provided for the third parameter';
		END IF;

		-- only use the first raster passed to this function
		IF array_length(_value, 1) > 1 THEN
			RAISE NOTICE 'Only using the values from the first raster';
		END IF;
		z := array_lower(_value, 1);

		_width := userargs[1]::double precision;
		_height := userargs[2]::double precision;
		_units := userargs[3];

		
		-- check that center pixel isn't NODATA
		IF _value[z][2][2] IS NULL THEN
			RETURN NULL;
		-- substitute center pixel for any neighbor pixels that are NODATA
		ELSE
			FOR y IN 1..3 LOOP
				FOR x IN 1..3 LOOP
					IF _value[z][y][x] IS NULL THEN
						_value[z][y][x] = _value[z][2][2];
					END IF;
				END LOOP;
			END LOOP;
		END IF;

		dz_dy := ((_value[z][3][1] + _value[z][3][2] + _value[z][3][2] + _value[z][3][3]) -
			(_value[z][1][1] + _value[z][1][2] + _value[z][1][2] + _value[z][1][3]));
		dz_dx := ((_value[z][1][3] + _value[z][2][3] + _value[z][2][3] + _value[z][3][3]) -
			(_value[z][1][1] + _value[z][2][1] + _value[z][2][1] + _value[z][3][1]));

		-- aspect is flat
		IF abs(dz_dx) = 0::double precision AND abs(dz_dy) = 0::double precision THEN
			RETURN -1;
		END IF;

		-- aspect is in radians
		aspect := atan2(dz_dy, -dz_dx);

		-- north = 0, pi/2 = east, 3pi/2 = west
		halfpi := pi() / 2.0;
		IF aspect > halfpi THEN
			aspect := (5.0 * halfpi) - aspect;
		ELSE
			aspect := halfpi - aspect;
		END IF;

		IF aspect = 2 * pi() THEN
			aspect := 0.;
		END IF;

		-- output depends on user preference
		CASE substring(upper(trim(leading from _units)) for 3)
			-- radians
			WHEN 'rad' THEN
				RETURN aspect;
			-- degrees (default)
			ELSE
				RETURN degrees(aspect);
		END CASE;

	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_asraster(geom geometry, scalex double precision DEFAULT 0, scaley double precision DEFAULT 0, width integer DEFAULT 0, height integer DEFAULT 0, pixeltype text[] DEFAULT ARRAY['8BUI'::text], value double precision[] DEFAULT ARRAY[(1)::double precision], nodataval double precision[] DEFAULT ARRAY[(0)::double precision], upperleftx double precision DEFAULT NULL::double precision, upperlefty double precision DEFAULT NULL::double precision, gridx double precision DEFAULT NULL::double precision, gridy double precision DEFAULT NULL::double precision, skewx double precision DEFAULT 0, skewy double precision DEFAULT 0, touched boolean DEFAULT false)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_asRaster$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_asx3d(integer, geometry, integer, integer, text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$LWGEOM_asX3D$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_bestsrid(geography)
 RETURNS integer
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public._ST_BestSRID($1,$1)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_bestsrid(geography, geography)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_bestsrid$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_buffer(geometry, double precision, cstring)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$buffer$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_clip(rast raster, nband integer[], geom geometry, nodataval double precision[] DEFAULT NULL::double precision[], crop boolean DEFAULT true)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_clip$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_colormap(rast raster, nband integer, colormap text, method text DEFAULT 'INTERPOLATE'::text)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_colorMap$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_concavehull(param_inputgeom geometry)
 RETURNS geometry
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
	DECLARE
	vexhull public.geometry;
	var_resultgeom public.geometry;
	var_inputgeom public.geometry;
	vexring public.geometry;
	cavering public.geometry;
	cavept public.geometry[];
	seglength double precision;
	var_tempgeom public.geometry;
	scale_factor float := 1;
	i integer;
	BEGIN
		-- First compute the ConvexHull of the geometry
		vexhull := public.ST_ConvexHull(param_inputgeom);
		var_inputgeom := param_inputgeom;
		--A point really has no concave hull
		IF public.ST_GeometryType(vexhull) = 'ST_Point' OR public.ST_GeometryType(vexHull) = 'ST_LineString' THEN
			RETURN vexhull;
		END IF;

		-- convert the hull perimeter to a linestring so we can manipulate individual points
		vexring := CASE WHEN public.ST_GeometryType(vexhull) = 'ST_LineString' THEN vexhull ELSE public.ST_ExteriorRing(vexhull) END;
		IF abs(public.ST_X(public.ST_PointN(vexring,1))) < 1 THEN --scale the geometry to prevent stupid precision errors - not sure it works so make low for now
			scale_factor := 100;
			vexring := public.ST_Scale(vexring, scale_factor,scale_factor);
			var_inputgeom := public.ST_Scale(var_inputgeom, scale_factor, scale_factor);
			--RAISE NOTICE 'Scaling';
		END IF;
		seglength := public.ST_Length(vexring)/least(public.ST_NPoints(vexring)*2,1000) ;

		vexring := public.ST_Segmentize(vexring, seglength);
		-- find the point on the original geom that is closest to each point of the convex hull and make a new linestring out of it.
		cavering := public.ST_Collect(
			ARRAY(

				SELECT
					public.ST_ClosestPoint(var_inputgeom, pt ) As the_geom
					FROM (
						SELECT  public.ST_PointN(vexring, n ) As pt, n
							FROM
							generate_series(1, public.ST_NPoints(vexring) ) As n
						) As pt

				)
			)
		;

		var_resultgeom := public.ST_MakeLine(geom)
			FROM public.ST_Dump(cavering) As foo;

		IF public.ST_IsSimple(var_resultgeom) THEN
			var_resultgeom := public.ST_MakePolygon(var_resultgeom);
			--RAISE NOTICE 'is Simple: %', var_resultgeom;
		ELSE 
			--RAISE NOTICE 'is not Simple: %', var_resultgeom;
			var_resultgeom := public.ST_ConvexHull(var_resultgeom);
		END IF;

		IF scale_factor > 1 THEN -- scale the result back
			var_resultgeom := public.ST_Scale(var_resultgeom, 1/scale_factor, 1/scale_factor);
		END IF;

		-- make sure result covers original (#3638)
		-- Using ST_UnaryUnion since SFCGAL doesn't replace with its own implementation
		-- and SFCGAL one chokes for some reason
		var_resultgeom := public.ST_UnaryUnion(public.ST_Collect(param_inputgeom, var_resultgeom) );
		RETURN var_resultgeom;

	END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_contains(rast1 raster, nband1 integer, rast2 raster, nband2 integer)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 1000
AS '$libdir/rtpostgis-2.5', $function$RASTER_contains$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_contains(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$contains$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_containsproperly(rast1 raster, nband1 integer, rast2 raster, nband2 integer)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 1000
AS '$libdir/rtpostgis-2.5', $function$RASTER_containsProperly$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_containsproperly(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$containsproperly$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_convertarray4ma(value double precision[])
 RETURNS double precision[]
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
	DECLARE
		_value double precision[][][];
		x int;
		y int;
	BEGIN
		IF array_ndims(value) != 2 THEN
			RAISE EXCEPTION 'Function parameter must be a 2-dimension array';
		END IF;

		_value := array_fill(NULL::double precision, ARRAY[1, array_length(value, 1), array_length(value, 2)]::int[], ARRAY[1, array_lower(value, 1), array_lower(value, 2)]::int[]);

		-- row
		FOR y IN array_lower(value, 1)..array_upper(value, 1) LOOP
			-- column
			FOR x IN array_lower(value, 2)..array_upper(value, 2) LOOP
				_value[1][y][x] = value[y][x];
			END LOOP;
		END LOOP;

		RETURN _value;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_count(rast raster, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, sample_percent double precision DEFAULT 1)
 RETURNS bigint
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
	DECLARE
		rtn bigint;
	BEGIN
		IF exclude_nodata_value IS FALSE THEN
			SELECT width * height INTO rtn FROM public.ST_Metadata(rast);
		ELSE
			SELECT count INTO rtn FROM public._ST_summarystats($1, $2, $3, $4);
		END IF;

		RETURN rtn;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_count(rastertable text, rastercolumn text, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, sample_percent double precision DEFAULT 1)
 RETURNS bigint
 LANGUAGE plpgsql
 STABLE STRICT
AS $function$
	DECLARE
		count bigint;
	BEGIN
		EXECUTE 'SELECT public.ST_CountAgg('
			|| quote_ident($2) || ', '
			|| $3 || ', '
			|| $4 || ', '
			|| $5 || ') '
			|| 'FROM ' || quote_ident($1)
	 	INTO count;
		RETURN count;
	END;
 	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_countagg_finalfn(agg agg_count)
 RETURNS bigint
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	BEGIN
		IF agg IS NULL THEN
			RAISE EXCEPTION 'Cannot count coverage';
		END IF;

		RETURN agg.count;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_countagg_transfn(agg agg_count, rast raster, exclude_nodata_value boolean)
 RETURNS agg_count
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		rtn_agg agg_count;
	BEGIN
		rtn_agg :=  public.__ST_countagg_transfn(
			agg,
			rast,
			1, exclude_nodata_value,
			1
		);
		RETURN rtn_agg;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_countagg_transfn(agg agg_count, rast raster, nband integer, exclude_nodata_value boolean)
 RETURNS agg_count
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		rtn_agg agg_count;
	BEGIN
		rtn_agg :=  public.__ST_countagg_transfn(
			agg,
			rast,
			nband, exclude_nodata_value,
			1
		);
		RETURN rtn_agg;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_countagg_transfn(agg agg_count, rast raster, nband integer, exclude_nodata_value boolean, sample_percent double precision)
 RETURNS agg_count
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		rtn_agg agg_count;
	BEGIN
		rtn_agg :=  public.__st_countagg_transfn(
			agg,
			rast,
			nband, exclude_nodata_value,
			sample_percent
		);
		RETURN rtn_agg;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_coveredby(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$coveredby$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_coveredby(rast1 raster, nband1 integer, rast2 raster, nband2 integer)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 1000
AS '$libdir/rtpostgis-2.5', $function$RASTER_coveredby$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_covers(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$covers$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_covers(rast1 raster, nband1 integer, rast2 raster, nband2 integer)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 1000
AS '$libdir/rtpostgis-2.5', $function$RASTER_covers$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_covers(geography, geography)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$geography_covers$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_crosses(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$crosses$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_dfullywithin(rast1 raster, nband1 integer, rast2 raster, nband2 integer, distance double precision)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 1000
AS '$libdir/rtpostgis-2.5', $function$RASTER_dfullywithin$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_dfullywithin(geom1 geometry, geom2 geometry, double precision)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_dfullywithin$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_distance(geography, geography, double precision, boolean)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$geography_distance$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_distancetree(geography, geography)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public._ST_DistanceTree($1, $2, 0.0, true)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_distancetree(geography, geography, double precision, boolean)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$geography_distance_tree$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_distanceuncached(geography, geography, boolean)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public._ST_DistanceUnCached($1, $2, 0.0, $3)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_distanceuncached(geography, geography, double precision, boolean)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$geography_distance_uncached$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_distanceuncached(geography, geography)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public._ST_DistanceUnCached($1, $2, 0.0, true)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_dwithin(rast1 raster, nband1 integer, rast2 raster, nband2 integer, distance double precision)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 1000
AS '$libdir/rtpostgis-2.5', $function$RASTER_dwithin$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_dwithin(geography, geography, double precision, boolean)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$geography_dwithin$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_dwithin(geom1 geometry, geom2 geometry, double precision)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$LWGEOM_dwithin$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_dwithinuncached(geography, geography, double precision)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.&&) public._ST_Expand($2,$3) AND $2 OPERATOR(public.&&) public._ST_Expand($1,$3) AND public._ST_DWithinUnCached($1, $2, $3, true)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_dwithinuncached(geography, geography, double precision, boolean)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$geography_dwithin_uncached$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_equals(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$ST_Equals$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_expand(geography, double precision)
 RETURNS geography
 LANGUAGE c
 IMMUTABLE STRICT COST 50
AS '$libdir/postgis-2.5', $function$geography_expand$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_gdalwarp(rast raster, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125, srid integer DEFAULT NULL::integer, scalex double precision DEFAULT 0, scaley double precision DEFAULT 0, gridx double precision DEFAULT NULL::double precision, gridy double precision DEFAULT NULL::double precision, skewx double precision DEFAULT 0, skewy double precision DEFAULT 0, width integer DEFAULT NULL::integer, height integer DEFAULT NULL::integer)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_GDALWarp$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_geomfromgml(text, integer)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$geom_from_gml$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_grayscale4ma(value double precision[], pos integer[], VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		ndims integer;
		_value double precision[][][];

		red double precision;
		green double precision;
		blue double precision;

		gray double precision;
	BEGIN

		ndims := array_ndims(value);
		-- add a third dimension if 2-dimension
		IF ndims = 2 THEN
			_value := public._ST_convertarray4ma(value);
		ELSEIF ndims != 3 THEN
			RAISE EXCEPTION 'First parameter of function must be a 3-dimension array';
		ELSE
			_value := value;
		END IF;

		red := _value[1][1][1];
		green := _value[2][1][1];
		blue := _value[3][1][1];

		gray = round(0.2989 * red + 0.5870 * green + 0.1140 * blue);
		RETURN gray;

	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_hillshade4ma(value double precision[], pos integer[], VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		_pixwidth double precision;
		_pixheight double precision;
		_width double precision;
		_height double precision;
		_azimuth double precision;
		_altitude double precision;
		_bright double precision;
		_scale double precision;

		dz_dx double precision;
		dz_dy double precision;
		azimuth double precision;
		zenith double precision;
		slope double precision;
		aspect double precision;
		shade double precision;

		_value double precision[][][];
		ndims int;
		z int;
	BEGIN
		ndims := array_ndims(value);
		-- add a third dimension if 2-dimension
		IF ndims = 2 THEN
			_value := public._ST_convertarray4ma(value);
		ELSEIF ndims != 3 THEN
			RAISE EXCEPTION 'First parameter of function must be a 3-dimension array';
		ELSE
			_value := value;
		END IF;

		IF (
			array_lower(_value, 2) != 1 OR array_upper(_value, 2) != 3 OR
			array_lower(_value, 3) != 1 OR array_upper(_value, 3) != 3
		) THEN
			RAISE EXCEPTION 'First parameter of function must be a 1x3x3 array with each of the lower bounds starting from 1';
		END IF;

		IF array_length(userargs, 1) < 8 THEN
			RAISE EXCEPTION 'At least eight elements must be provided for the third parameter';
		END IF;

		-- only use the first raster passed to this function
		IF array_length(_value, 1) > 1 THEN
			RAISE NOTICE 'Only using the values from the first raster';
		END IF;
		z := array_lower(_value, 1);

		_pixwidth := userargs[1]::double precision;
		_pixheight := userargs[2]::double precision;
		_width := userargs[3]::double precision;
		_height := userargs[4]::double precision;
		_azimuth := userargs[5]::double precision;
		_altitude := userargs[6]::double precision;
		_bright := userargs[7]::double precision;
		_scale := userargs[8]::double precision;

		-- check that pixel is not edge pixel
		IF (pos[1][1] = 1 OR pos[1][2] = 1) OR (pos[1][1] = _width OR pos[1][2] = _height) THEN
			RETURN NULL;
		END IF;

		-- clamp azimuth
		IF _azimuth < 0. THEN
			RAISE NOTICE 'Clamping provided azimuth value % to 0', _azimuth;
			_azimuth := 0.;
		ELSEIF _azimuth >= 360. THEN
			RAISE NOTICE 'Converting provided azimuth value % to be between 0 and 360', _azimuth;
			_azimuth := _azimuth - (360. * floor(_azimuth / 360.));
		END IF;
		azimuth := 360. - _azimuth + 90.;
		IF azimuth >= 360. THEN
			azimuth := azimuth - 360.;
		END IF;
		azimuth := radians(azimuth);
		--RAISE NOTICE 'azimuth = %', azimuth;

		-- clamp altitude
		IF _altitude < 0. THEN
			RAISE NOTICE 'Clamping provided altitude value % to 0', _altitude;
			_altitude := 0.;
		ELSEIF _altitude > 90. THEN
			RAISE NOTICE 'Clamping provided altitude value % to 90', _altitude;
			_altitude := 90.;
		END IF;
		zenith := radians(90. - _altitude);
		--RAISE NOTICE 'zenith = %', zenith;

		-- clamp bright
		IF _bright < 0. THEN
			RAISE NOTICE 'Clamping provided bright value % to 0', _bright;
			_bright := 0.;
		ELSEIF _bright > 255. THEN
			RAISE NOTICE 'Clamping provided bright value % to 255', _bright;
			_bright := 255.;
		END IF;

		dz_dy := ((_value[z][3][1] + _value[z][3][2] + _value[z][3][2] + _value[z][3][3]) -
			(_value[z][1][1] + _value[z][1][2] + _value[z][1][2] + _value[z][1][3])) / (8 * _pixheight);
		dz_dx := ((_value[z][1][3] + _value[z][2][3] + _value[z][2][3] + _value[z][3][3]) -
			(_value[z][1][1] + _value[z][2][1] + _value[z][2][1] + _value[z][3][1])) / (8 * _pixwidth);

		slope := atan(sqrt(dz_dx * dz_dx + dz_dy * dz_dy) / _scale);

		IF dz_dx != 0. THEN
			aspect := atan2(dz_dy, -dz_dx);

			IF aspect < 0. THEN
				aspect := aspect + (2.0 * pi());
			END IF;
		ELSE
			IF dz_dy > 0. THEN
				aspect := pi() / 2.;
			ELSEIF dz_dy < 0. THEN
				aspect := (2. * pi()) - (pi() / 2.);
			-- set to pi as that is the expected PostgreSQL answer in Linux
			ELSE
				aspect := pi();
			END IF;
		END IF;

		shade := _bright * ((cos(zenith) * cos(slope)) + (sin(zenith) * sin(slope) * cos(azimuth - aspect)));

		IF shade < 0. THEN
			shade := 0;
		END IF;

		RETURN shade;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_histogram(rastertable text, rastercolumn text, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, sample_percent double precision DEFAULT 1, bins integer DEFAULT 0, width double precision[] DEFAULT NULL::double precision[], "right" boolean DEFAULT false, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_histogramCoverage$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_histogram(rast raster, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, sample_percent double precision DEFAULT 1, bins integer DEFAULT 0, width double precision[] DEFAULT NULL::double precision[], "right" boolean DEFAULT false, min double precision DEFAULT NULL::double precision, max double precision DEFAULT NULL::double precision, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_histogram$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_intersects(rast1 raster, nband1 integer, rast2 raster, nband2 integer)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 1000
AS '$libdir/rtpostgis-2.5', $function$RASTER_intersects$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_intersects(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$intersects$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_intersects(geom geometry, rast raster, nband integer DEFAULT NULL::integer)
 RETURNS boolean
 LANGUAGE plpgsql
 IMMUTABLE COST 1000
AS $function$
	DECLARE
		hasnodata boolean := TRUE;
		_geom public.geometry;
	BEGIN
		IF public.ST_SRID(rast) != public.ST_SRID(geom) THEN
			RAISE EXCEPTION 'Raster and geometry do not have the same SRID';
		END IF;

		_geom := public.ST_ConvexHull(rast);
		IF nband IS NOT NULL THEN
			SELECT CASE WHEN bmd.nodatavalue IS NULL THEN FALSE ELSE NULL END INTO hasnodata FROM public.ST_BandMetaData(rast, nband) AS bmd;
		END IF;

		IF public.ST_Intersects(geom, _geom) IS NOT TRUE THEN
			RETURN FALSE;
		ELSEIF nband IS NULL OR hasnodata IS FALSE THEN
			RETURN TRUE;
		END IF;

		SELECT public.ST_Buffer(public.ST_Collect(t.geom), 0) INTO _geom FROM public.ST_PixelAsPolygons(rast, nband) AS t;
		RETURN public.ST_Intersects(geom, _geom);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_linecrossingdirection(geom1 geometry, geom2 geometry)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$ST_LineCrossingDirection$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_longestline(geom1 geometry, geom2 geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_longestline2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_mapalgebra(rastbandargset rastbandarg[], callbackfunc regprocedure, pixeltype text DEFAULT NULL::text, distancex integer DEFAULT 0, distancey integer DEFAULT 0, extenttype text DEFAULT 'INTERSECTION'::text, customextent raster DEFAULT NULL::raster, mask double precision[] DEFAULT NULL::double precision[], weighted boolean DEFAULT NULL::boolean, VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_nMapAlgebra$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_mapalgebra(rastbandargset rastbandarg[], expression text, pixeltype text DEFAULT NULL::text, extenttype text DEFAULT 'INTERSECTION'::text, nodata1expr text DEFAULT NULL::text, nodata2expr text DEFAULT NULL::text, nodatanodataval double precision DEFAULT NULL::double precision)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_nMapAlgebraExpr$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_maxdistance(geom1 geometry, geom2 geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_maxdistance2d_linestring$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_neighborhood(rast raster, band integer, columnx integer, rowy integer, distancex integer, distancey integer, exclude_nodata_value boolean DEFAULT true)
 RETURNS double precision[]
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_neighborhood$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_orderingequals(geometrya geometry, geometryb geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$LWGEOM_same$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_overlaps(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$overlaps$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_overlaps(rast1 raster, nband1 integer, rast2 raster, nband2 integer)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 1000
AS '$libdir/rtpostgis-2.5', $function$RASTER_overlaps$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_pixelaspolygons(rast raster, band integer DEFAULT 1, columnx integer DEFAULT NULL::integer, rowy integer DEFAULT NULL::integer, exclude_nodata_value boolean DEFAULT true, OUT geom geometry, OUT val double precision, OUT x integer, OUT y integer)
 RETURNS SETOF record
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_getPixelPolygons$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_pointoutside(geography)
 RETURNS geography
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_point_outside$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_quantile(rastertable text, rastercolumn text, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, sample_percent double precision DEFAULT 1, quantiles double precision[] DEFAULT NULL::double precision[], OUT quantile double precision, OUT value double precision)
 RETURNS SETOF record
 LANGUAGE c
 STABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_quantileCoverage$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_quantile(rast raster, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, sample_percent double precision DEFAULT 1, quantiles double precision[] DEFAULT NULL::double precision[], OUT quantile double precision, OUT value double precision)
 RETURNS SETOF record
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_quantile$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_rastertoworldcoord(rast raster, columnx integer DEFAULT NULL::integer, rowy integer DEFAULT NULL::integer, OUT longitude double precision, OUT latitude double precision)
 RETURNS record
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_rasterToWorldCoord$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_reclass(rast raster, VARIADIC reclassargset reclassarg[])
 RETURNS raster
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_reclass$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_roughness4ma(value double precision[], pos integer[], VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		x integer;
		y integer;
		z integer;

		minimum double precision;
		maximum double precision;

		_value double precision[][][];
		ndims int;
	BEGIN

		ndims := array_ndims(value);
		-- add a third dimension if 2-dimension
		IF ndims = 2 THEN
			_value := public._ST_convertarray4ma(value);
		ELSEIF ndims != 3 THEN
			RAISE EXCEPTION 'First parameter of function must be a 3-dimension array';
		ELSE
			_value := value;
		END IF;

		-- only use the first raster passed to this function
		IF array_length(_value, 1) > 1 THEN
			RAISE NOTICE 'Only using the values from the first raster';
		END IF;
		z := array_lower(_value, 1);

		IF (
			array_lower(_value, 2) != 1 OR array_upper(_value, 2) != 3 OR
			array_lower(_value, 3) != 1 OR array_upper(_value, 3) != 3
		) THEN
			RAISE EXCEPTION 'First parameter of function must be a 1x3x3 array with each of the lower bounds starting from 1';
		END IF;

		-- check that center pixel isn't NODATA
		IF _value[z][2][2] IS NULL THEN
			RETURN NULL;
		-- substitute center pixel for any neighbor pixels that are NODATA
		ELSE
			FOR y IN 1..3 LOOP
				FOR x IN 1..3 LOOP
					IF _value[z][y][x] IS NULL THEN
						_value[z][y][x] = _value[z][2][2];
					END IF;
				END LOOP;
			END LOOP;
		END IF;

		minimum := _value[z][1][1];
		maximum := _value[z][1][1];

		FOR Y IN 1..3 LOOP
		    FOR X IN 1..3 LOOP
		    	 IF _value[z][y][x] < minimum THEN
			    minimum := _value[z][y][x];
			 ELSIF _value[z][y][x] > maximum THEN
			    maximum := _value[z][y][x];
			 END IF;
		    END LOOP;
		END LOOP;

		RETURN maximum - minimum;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_samealignment_finalfn(agg agg_samealignment)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT $1.aligned $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_samealignment_transfn(agg agg_samealignment, rast raster)
 RETURNS agg_samealignment
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		m record;
		aligned boolean;
	BEGIN
		IF agg IS NULL THEN
			agg.refraster := NULL;
			agg.aligned := NULL;
		END IF;

		IF rast IS NULL THEN
			agg.aligned := NULL;
		ELSE
			IF agg.refraster IS NULL THEN
				m := ST_Metadata(rast);
				agg.refraster := ST_MakeEmptyRaster(1, 1, m.upperleftx, m.upperlefty, m.scalex, m.scaley, m.skewx, m.skewy, m.srid);
				agg.aligned := TRUE;
			ELSIF agg.aligned IS TRUE THEN
				agg.aligned := ST_SameAlignment(agg.refraster, rast);
			END IF;
		END IF;
		RETURN agg;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_setvalues(rast raster, nband integer, x integer, y integer, newvalueset double precision[], noset boolean[] DEFAULT NULL::boolean[], hasnosetvalue boolean DEFAULT false, nosetvalue double precision DEFAULT NULL::double precision, keepnodata boolean DEFAULT false)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_setPixelValuesArray$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_slope4ma(value double precision[], pos integer[], VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		x integer;
		y integer;
		z integer;

		_pixwidth double precision;
		_pixheight double precision;
		_width double precision;
		_height double precision;
		_units text;
		_scale double precision;

		dz_dx double precision;
		dz_dy double precision;

		slope double precision;

		_value double precision[][][];
		ndims int;
	BEGIN

		ndims := array_ndims(value);
		-- add a third dimension if 2-dimension
		IF ndims = 2 THEN
			_value := public._ST_convertarray4ma(value);
		ELSEIF ndims != 3 THEN
			RAISE EXCEPTION 'First parameter of function must be a 3-dimension array';
		ELSE
			_value := value;
		END IF;

		-- only use the first raster passed to this function
		IF array_length(_value, 1) > 1 THEN
			RAISE NOTICE 'Only using the values from the first raster';
		END IF;
		z := array_lower(_value, 1);

		IF (
			array_lower(_value, 2) != 1 OR array_upper(_value, 2) != 3 OR
			array_lower(_value, 3) != 1 OR array_upper(_value, 3) != 3
		) THEN
			RAISE EXCEPTION 'First parameter of function must be a 1x3x3 array with each of the lower bounds starting from 1';
		END IF;

		IF array_length(userargs, 1) < 6 THEN
			RAISE EXCEPTION 'At least six elements must be provided for the third parameter';
		END IF;

		_pixwidth := userargs[1]::double precision;
		_pixheight := userargs[2]::double precision;
		_width := userargs[3]::double precision;
		_height := userargs[4]::double precision;
		_units := userargs[5];
		_scale := userargs[6]::double precision;

		
		-- check that center pixel isn't NODATA
		IF _value[z][2][2] IS NULL THEN
			RETURN NULL;
		-- substitute center pixel for any neighbor pixels that are NODATA
		ELSE
			FOR y IN 1..3 LOOP
				FOR x IN 1..3 LOOP
					IF _value[z][y][x] IS NULL THEN
						_value[z][y][x] = _value[z][2][2];
					END IF;
				END LOOP;
			END LOOP;
		END IF;

		dz_dy := ((_value[z][3][1] + _value[z][3][2] + _value[z][3][2] + _value[z][3][3]) -
			(_value[z][1][1] + _value[z][1][2] + _value[z][1][2] + _value[z][1][3])) / _pixheight;
		dz_dx := ((_value[z][1][3] + _value[z][2][3] + _value[z][2][3] + _value[z][3][3]) -
			(_value[z][1][1] + _value[z][2][1] + _value[z][2][1] + _value[z][3][1])) / _pixwidth;

		slope := sqrt(dz_dx * dz_dx + dz_dy * dz_dy) / (8 * _scale);

		-- output depends on user preference
		CASE substring(upper(trim(leading from _units)) for 3)
			-- percentages
			WHEN 'PER' THEN
				slope := 100.0 * slope;
			-- radians
			WHEN 'rad' THEN
				slope := atan(slope);
			-- degrees (default)
			ELSE
				slope := degrees(atan(slope));
		END CASE;

		RETURN slope;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_summarystats(rast raster, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, sample_percent double precision DEFAULT 1)
 RETURNS summarystats
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_summaryStats$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_summarystats(rastertable text, rastercolumn text, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, sample_percent double precision DEFAULT 1)
 RETURNS summarystats
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		stats summarystats;
	BEGIN
		EXECUTE 'SELECT (stats).* FROM (SELECT public.ST_SummaryStatsAgg('
			|| quote_ident($2) || ', '
			|| $3 || ', '
			|| $4 || ', '
			|| $5 || ') AS stats '
			|| 'FROM ' || quote_ident($1)
			|| ') foo'
			INTO stats;
		RETURN stats;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_summarystats_finalfn(internal)
 RETURNS summarystats
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_summaryStats_finalfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_summarystats_transfn(internal, raster, integer, boolean)
 RETURNS internal
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_summaryStats_transfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_summarystats_transfn(internal, raster, boolean, double precision)
 RETURNS internal
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_summaryStats_transfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_summarystats_transfn(internal, raster, integer, boolean, double precision)
 RETURNS internal
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_summaryStats_transfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_tile(rast raster, width integer, height integer, nband integer[] DEFAULT NULL::integer[], padwithnodata boolean DEFAULT false, nodataval double precision DEFAULT NULL::double precision)
 RETURNS SETOF raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_tile$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_touches(rast1 raster, nband1 integer, rast2 raster, nband2 integer)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 1000
AS '$libdir/rtpostgis-2.5', $function$RASTER_touches$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_touches(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$touches$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_tpi4ma(value double precision[], pos integer[], VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		x integer;
		y integer;
		z integer;

		Z1 double precision;
		Z2 double precision;
		Z3 double precision;
		Z4 double precision;
		Z5 double precision;
		Z6 double precision;
		Z7 double precision;
		Z8 double precision;
		Z9 double precision;

		tpi double precision;
		mean double precision;
		_value double precision[][][];
		ndims int;
	BEGIN
		ndims := array_ndims(value);
		-- add a third dimension if 2-dimension
		IF ndims = 2 THEN
			_value := public._ST_convertarray4ma(value);
		ELSEIF ndims != 3 THEN
			RAISE EXCEPTION 'First parameter of function must be a 3-dimension array';
		ELSE
			_value := value;
		END IF;

		-- only use the first raster passed to this function
		IF array_length(_value, 1) > 1 THEN
			RAISE NOTICE 'Only using the values from the first raster';
		END IF;
		z := array_lower(_value, 1);

		IF (
			array_lower(_value, 2) != 1 OR array_upper(_value, 2) != 3 OR
			array_lower(_value, 3) != 1 OR array_upper(_value, 3) != 3
		) THEN
			RAISE EXCEPTION 'First parameter of function must be a 1x3x3 array with each of the lower bounds starting from 1';
		END IF;

		-- check that center pixel isn't NODATA
		IF _value[z][2][2] IS NULL THEN
			RETURN NULL;
		-- substitute center pixel for any neighbor pixels that are NODATA
		ELSE
			FOR y IN 1..3 LOOP
				FOR x IN 1..3 LOOP
					IF _value[z][y][x] IS NULL THEN
						_value[z][y][x] = _value[z][2][2];
					END IF;
				END LOOP;
			END LOOP;
		END IF;

		-------------------------------------------------
		--|   Z1= Z(-1,1) |  Z2= Z(0,1)	| Z3= Z(1,1)  |--
		-------------------------------------------------
		--|   Z4= Z(-1,0) |  Z5= Z(0,0) | Z6= Z(1,0)  |--
		-------------------------------------------------
		--|   Z7= Z(-1,-1)|  Z8= Z(0,-1)|  Z9= Z(1,-1)|--
		-------------------------------------------------

		Z1 := _value[z][1][1];
		Z2 := _value[z][2][1];
		Z3 := _value[z][3][1];
		Z4 := _value[z][1][2];
		Z5 := _value[z][2][2];
		Z6 := _value[z][3][2];
		Z7 := _value[z][1][3];
		Z8 := _value[z][2][3];
		Z9 := _value[z][3][3];

		mean := (Z1 + Z2 + Z3 + Z4 + Z6 + Z7 + Z8 + Z9)/8;
		tpi := Z5-mean;

		return tpi;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_tri4ma(value double precision[], pos integer[], VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		x integer;
		y integer;
		z integer;

		Z1 double precision;
		Z2 double precision;
		Z3 double precision;
		Z4 double precision;
		Z5 double precision;
		Z6 double precision;
		Z7 double precision;
		Z8 double precision;
		Z9 double precision;

		tri double precision;
		_value double precision[][][];
		ndims int;
	BEGIN
		ndims := array_ndims(value);
		-- add a third dimension if 2-dimension
		IF ndims = 2 THEN
			_value := public._ST_convertarray4ma(value);
		ELSEIF ndims != 3 THEN
			RAISE EXCEPTION 'First parameter of function must be a 3-dimension array';
		ELSE
			_value := value;
		END IF;

		-- only use the first raster passed to this function
		IF array_length(_value, 1) > 1 THEN
			RAISE NOTICE 'Only using the values from the first raster';
		END IF;
		z := array_lower(_value, 1);

		IF (
			array_lower(_value, 2) != 1 OR array_upper(_value, 2) != 3 OR
			array_lower(_value, 3) != 1 OR array_upper(_value, 3) != 3
		) THEN
			RAISE EXCEPTION 'First parameter of function must be a 1x3x3 array with each of the lower bounds starting from 1';
		END IF;

		-- check that center pixel isn't NODATA
		IF _value[z][2][2] IS NULL THEN
			RETURN NULL;
		-- substitute center pixel for any neighbor pixels that are NODATA
		ELSE
			FOR y IN 1..3 LOOP
				FOR x IN 1..3 LOOP
					IF _value[z][y][x] IS NULL THEN
						_value[z][y][x] = _value[z][2][2];
					END IF;
				END LOOP;
			END LOOP;
		END IF;

		-------------------------------------------------
		--|   Z1= Z(-1,1) |  Z2= Z(0,1)	| Z3= Z(1,1)  |--
		-------------------------------------------------
		--|   Z4= Z(-1,0) |  Z5= Z(0,0) | Z6= Z(1,0)  |--
		-------------------------------------------------
		--|   Z7= Z(-1,-1)|  Z8= Z(0,-1)|  Z9= Z(1,-1)|--
		-------------------------------------------------

		-- _scale width and height units / z units to make z units equal to height width units
		Z1 := _value[z][1][1];
		Z2 := _value[z][2][1];
		Z3 := _value[z][3][1];
		Z4 := _value[z][1][2];
		Z5 := _value[z][2][2];
		Z6 := _value[z][3][2];
		Z7 := _value[z][1][3];
		Z8 := _value[z][2][3];
		Z9 := _value[z][3][3];

		tri := ( abs(Z1 - Z5 ) + abs( Z2 - Z5 ) + abs( Z3 - Z5 ) + abs( Z4 - Z5 ) + abs( Z6 - Z5 ) + abs( Z7 - Z5 ) + abs( Z8 - Z5 ) + abs ( Z9 - Z5 )) / 8;

		return tri;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_union_finalfn(internal)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_union_finalfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_union_transfn(internal, raster, integer, text)
 RETURNS internal
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_union_transfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_union_transfn(internal, raster, unionarg[])
 RETURNS internal
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_union_transfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_union_transfn(internal, raster)
 RETURNS internal
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_union_transfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_union_transfn(internal, raster, text)
 RETURNS internal
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_union_transfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_union_transfn(internal, raster, integer)
 RETURNS internal
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_union_transfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_valuecount(rast raster, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, searchvalues double precision[] DEFAULT NULL::double precision[], roundto double precision DEFAULT 0, OUT value double precision, OUT count integer, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_valueCount$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_valuecount(rastertable text, rastercolumn text, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, searchvalues double precision[] DEFAULT NULL::double precision[], roundto double precision DEFAULT 0, OUT value double precision, OUT count integer, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE c
 STABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_valueCountCoverage$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_voronoi(g1 geometry, clip geometry DEFAULT NULL::geometry, tolerance double precision DEFAULT 0.0, return_polygons boolean DEFAULT true)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$ST_Voronoi$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_within(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT public._ST_Contains($2,$1)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_within(rast1 raster, nband1 integer, rast2 raster, nband2 integer)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT public._st_contains($3, $4, $1, $2) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._st_worldtorastercoord(rast raster, longitude double precision DEFAULT NULL::double precision, latitude double precision DEFAULT NULL::double precision, OUT columnx integer, OUT rowy integer)
 RETURNS record
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_worldToRasterCoord$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public._updaterastersrid(schema_name name, table_name name, column_name name, new_srid integer)
 RETURNS boolean
 LANGUAGE plpgsql
AS $function$
	DECLARE
		fqtn text;
		schema name;
		sql text;
		srid integer;
		ct boolean;
	BEGIN
		-- validate schema
		schema := NULL;
		IF length($1) > 0 THEN
			sql := 'SELECT nspname FROM pg_namespace '
				|| 'WHERE nspname = ' || quote_literal($1)
				|| 'LIMIT 1';
			EXECUTE sql INTO schema;

			IF schema IS NULL THEN
				RAISE EXCEPTION 'The value provided for schema is invalid';
				RETURN FALSE;
			END IF;
		END IF;

		IF schema IS NULL THEN
			sql := 'SELECT n.nspname AS schemaname '
				|| 'FROM pg_catalog.pg_class c '
				|| 'JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace '
				|| 'WHERE c.relkind = ' || quote_literal('r')
				|| ' AND n.nspname NOT IN (' || quote_literal('pg_catalog')
				|| ', ' || quote_literal('pg_toast')
				|| ') AND pg_catalog.pg_table_is_visible(c.oid)'
				|| ' AND c.relname = ' || quote_literal($2);
			EXECUTE sql INTO schema;

			IF schema IS NULL THEN
				RAISE EXCEPTION 'The table % does not occur in the search_path', quote_literal($2);
				RETURN FALSE;
			END IF;
		END IF;

		-- clamp SRID
		IF new_srid < 0 THEN
			srid :=  public.ST_SRID('POINT EMPTY'::public.geometry);
			RAISE NOTICE 'SRID % converted to the officially unknown SRID %', new_srid, srid;
		ELSE
			srid := new_srid;
		END IF;

		-- drop coverage tile constraint
		-- done separately just in case constraint doesn't exist
		ct := public._raster_constraint_info_coverage_tile(schema, $2, $3);
		IF ct IS TRUE THEN
			PERFORM  public._drop_raster_constraint_coverage_tile(schema, $2, $3);
		END IF;

		-- drop SRID, extent, alignment constraints
		PERFORM  public.DropRasterConstraints(schema, $2, $3, 'extent', 'alignment', 'srid');

		fqtn := '';
		IF length($1) > 0 THEN
			fqtn := quote_ident($1) || '.';
		END IF;
		fqtn := fqtn || quote_ident($2);

		-- update SRID
		sql := 'UPDATE ' || fqtn ||
			' SET ' || quote_ident($3) ||
			' =  public.ST_SetSRID(' || quote_ident($3) ||
			'::public.raster, ' || srid || ')';
		RAISE NOTICE 'sql = %', sql;
		EXECUTE sql;

		-- add SRID constraint
		PERFORM  public.AddRasterConstraints(schema, $2, $3, 'srid', 'extent', 'alignment');

		-- add coverage tile constraint if needed
		IF ct IS TRUE THEN
			PERFORM  public._add_raster_constraint_coverage_tile(schema, $2, $3);
		END IF;

		RETURN TRUE;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.addauth(text)
 RETURNS boolean
 LANGUAGE plpgsql
AS $function$
DECLARE
	lockid alias for $1;
	okay boolean;
	myrec record;
BEGIN
	-- check to see if table exists
	--  if not, CREATE TEMP TABLE mylock (transid xid, lockcode text)
	okay := 'f';
	FOR myrec IN SELECT * FROM pg_class WHERE relname = 'temp_lock_have_table' LOOP
		okay := 't';
	END LOOP;
	IF (okay <> 't') THEN
		CREATE TEMP TABLE temp_lock_have_table (transid xid, lockcode text);
			-- this will only work from pgsql7.4 up
			-- ON COMMIT DELETE ROWS;
	END IF;

	--  INSERT INTO mylock VALUES ( $1)
--	EXECUTE 'INSERT INTO temp_lock_have_table VALUES ( '||
--		quote_literal(getTransactionID()) || ',' ||
--		quote_literal(lockid) ||')';

	INSERT INTO temp_lock_have_table VALUES (getTransactionID(), lockid);

	RETURN true::boolean;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.addgeometrycolumn(catalog_name character varying, schema_name character varying, table_name character varying, column_name character varying, new_srid_in integer, new_type character varying, new_dim integer, use_typmod boolean DEFAULT true)
 RETURNS text
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
	rec RECORD;
	sr varchar;
	real_schema name;
	sql text;
	new_srid integer;

BEGIN

	-- Verify geometry type
	IF (postgis_type_name(new_type,new_dim) IS NULL )
	THEN
		RAISE EXCEPTION 'Invalid type name "%(%)" - valid ones are:
	POINT, MULTIPOINT,
	LINESTRING, MULTILINESTRING,
	POLYGON, MULTIPOLYGON,
	CIRCULARSTRING, COMPOUNDCURVE, MULTICURVE,
	CURVEPOLYGON, MULTISURFACE,
	GEOMETRY, GEOMETRYCOLLECTION,
	POINTM, MULTIPOINTM,
	LINESTRINGM, MULTILINESTRINGM,
	POLYGONM, MULTIPOLYGONM,
	CIRCULARSTRINGM, COMPOUNDCURVEM, MULTICURVEM
	CURVEPOLYGONM, MULTISURFACEM, TRIANGLE, TRIANGLEM,
	POLYHEDRALSURFACE, POLYHEDRALSURFACEM, TIN, TINM
	or GEOMETRYCOLLECTIONM', new_type, new_dim;
		RETURN 'fail';
	END IF;

	-- Verify dimension
	IF ( (new_dim >4) OR (new_dim <2) ) THEN
		RAISE EXCEPTION 'invalid dimension';
		RETURN 'fail';
	END IF;

	IF ( (new_type LIKE '%M') AND (new_dim!=3) ) THEN
		RAISE EXCEPTION 'TypeM needs 3 dimensions';
		RETURN 'fail';
	END IF;

	-- Verify SRID
	IF ( new_srid_in > 0 ) THEN
		IF new_srid_in > 998999 THEN
			RAISE EXCEPTION 'AddGeometryColumn() - SRID must be <= %', 998999;
		END IF;
		new_srid := new_srid_in;
		SELECT SRID INTO sr FROM spatial_ref_sys WHERE SRID = new_srid;
		IF NOT FOUND THEN
			RAISE EXCEPTION 'AddGeometryColumn() - invalid SRID';
			RETURN 'fail';
		END IF;
	ELSE
		new_srid := public.ST_SRID('POINT EMPTY'::public.geometry);
		IF ( new_srid_in != new_srid ) THEN
			RAISE NOTICE 'SRID value % converted to the officially unknown SRID value %', new_srid_in, new_srid;
		END IF;
	END IF;

	-- Verify schema
	IF ( schema_name IS NOT NULL AND schema_name != '' ) THEN
		sql := 'SELECT nspname FROM pg_namespace ' ||
			'WHERE text(nspname) = ' || quote_literal(schema_name) ||
			'LIMIT 1';
		RAISE DEBUG '%', sql;
		EXECUTE sql INTO real_schema;

		IF ( real_schema IS NULL ) THEN
			RAISE EXCEPTION 'Schema % is not a valid schemaname', quote_literal(schema_name);
			RETURN 'fail';
		END IF;
	END IF;

	IF ( real_schema IS NULL ) THEN
		RAISE DEBUG 'Detecting schema';
		sql := 'SELECT n.nspname AS schemaname ' ||
			'FROM pg_catalog.pg_class c ' ||
			  'JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace ' ||
			'WHERE c.relkind = ' || quote_literal('r') ||
			' AND n.nspname NOT IN (' || quote_literal('pg_catalog') || ', ' || quote_literal('pg_toast') || ')' ||
			' AND pg_catalog.pg_table_is_visible(c.oid)' ||
			' AND c.relname = ' || quote_literal(table_name);
		RAISE DEBUG '%', sql;
		EXECUTE sql INTO real_schema;

		IF ( real_schema IS NULL ) THEN
			RAISE EXCEPTION 'Table % does not occur in the search_path', quote_literal(table_name);
			RETURN 'fail';
		END IF;
	END IF;

	-- Add geometry column to table
	IF use_typmod THEN
	     sql := 'ALTER TABLE ' ||
            quote_ident(real_schema) || '.' || quote_ident(table_name)
            || ' ADD COLUMN ' || quote_ident(column_name) ||
            ' geometry(' || public.postgis_type_name(new_type, new_dim) || ', ' || new_srid::text || ')';
        RAISE DEBUG '%', sql;
	ELSE
        sql := 'ALTER TABLE ' ||
            quote_ident(real_schema) || '.' || quote_ident(table_name)
            || ' ADD COLUMN ' || quote_ident(column_name) ||
            ' geometry ';
        RAISE DEBUG '%', sql;
    END IF;
	EXECUTE sql;

	IF NOT use_typmod THEN
        -- Add table CHECKs
        sql := 'ALTER TABLE ' ||
            quote_ident(real_schema) || '.' || quote_ident(table_name)
            || ' ADD CONSTRAINT '
            || quote_ident('enforce_srid_' || column_name)
            || ' CHECK (st_srid(' || quote_ident(column_name) ||
            ') = ' || new_srid::text || ')' ;
        RAISE DEBUG '%', sql;
        EXECUTE sql;

        sql := 'ALTER TABLE ' ||
            quote_ident(real_schema) || '.' || quote_ident(table_name)
            || ' ADD CONSTRAINT '
            || quote_ident('enforce_dims_' || column_name)
            || ' CHECK (st_ndims(' || quote_ident(column_name) ||
            ') = ' || new_dim::text || ')' ;
        RAISE DEBUG '%', sql;
        EXECUTE sql;

        IF ( NOT (new_type = 'GEOMETRY')) THEN
            sql := 'ALTER TABLE ' ||
                quote_ident(real_schema) || '.' || quote_ident(table_name) || ' ADD CONSTRAINT ' ||
                quote_ident('enforce_geotype_' || column_name) ||
                ' CHECK (GeometryType(' ||
                quote_ident(column_name) || ')=' ||
                quote_literal(new_type) || ' OR (' ||
                quote_ident(column_name) || ') is null)';
            RAISE DEBUG '%', sql;
            EXECUTE sql;
        END IF;
    END IF;

	RETURN
		real_schema || '.' ||
		table_name || '.' || column_name ||
		' SRID:' || new_srid::text ||
		' TYPE:' || new_type ||
		' DIMS:' || new_dim::text || ' ';
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.addgeometrycolumn(schema_name character varying, table_name character varying, column_name character varying, new_srid integer, new_type character varying, new_dim integer, use_typmod boolean DEFAULT true)
 RETURNS text
 LANGUAGE plpgsql
 STABLE STRICT
AS $function$
DECLARE
	ret  text;
BEGIN
	SELECT public.AddGeometryColumn('',$1,$2,$3,$4,$5,$6,$7) into ret;
	RETURN ret;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.addgeometrycolumn(table_name character varying, column_name character varying, new_srid integer, new_type character varying, new_dim integer, use_typmod boolean DEFAULT true)
 RETURNS text
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
	ret  text;
BEGIN
	SELECT public.AddGeometryColumn('','',$1,$2,$3,$4,$5, $6) into ret;
	RETURN ret;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.addoverviewconstraints(ovtable name, ovcolumn name, reftable name, refcolumn name, ovfactor integer)
 RETURNS boolean
 LANGUAGE sql
 STRICT
AS $function$ SELECT  public.AddOverviewConstraints('', $1, $2, '', $3, $4, $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.addoverviewconstraints(ovschema name, ovtable name, ovcolumn name, refschema name, reftable name, refcolumn name, ovfactor integer)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		x int;
		s name;
		t name;
		oschema name;
		rschema name;
		sql text;
		rtn boolean;
	BEGIN
		FOR x IN 1..2 LOOP
			s := '';

			IF x = 1 THEN
				s := $1;
				t := $2;
			ELSE
				s := $4;
				t := $5;
			END IF;

			-- validate user-provided schema
			IF length(s) > 0 THEN
				sql := 'SELECT nspname FROM pg_namespace '
					|| 'WHERE nspname = ' || quote_literal(s)
					|| 'LIMIT 1';
				EXECUTE sql INTO s;

				IF s IS NULL THEN
					RAISE EXCEPTION 'The value % is not a valid schema', quote_literal(s);
					RETURN FALSE;
				END IF;
			END IF;

			-- no schema, determine what it could be using the table
			IF length(s) < 1 THEN
				sql := 'SELECT n.nspname AS schemaname '
					|| 'FROM pg_catalog.pg_class c '
					|| 'JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace '
					|| 'WHERE c.relkind = ' || quote_literal('r')
					|| ' AND n.nspname NOT IN (' || quote_literal('pg_catalog')
					|| ', ' || quote_literal('pg_toast')
					|| ') AND pg_catalog.pg_table_is_visible(c.oid)'
					|| ' AND c.relname = ' || quote_literal(t);
				EXECUTE sql INTO s;

				IF s IS NULL THEN
					RAISE EXCEPTION 'The table % does not occur in the search_path', quote_literal(t);
					RETURN FALSE;
				END IF;
			END IF;

			IF x = 1 THEN
				oschema := s;
			ELSE
				rschema := s;
			END IF;
		END LOOP;

		-- reference raster
		rtn :=  public._add_overview_constraint(oschema, $2, $3, rschema, $5, $6, $7);
		IF rtn IS FALSE THEN
			RAISE EXCEPTION 'Unable to add the overview constraint.  Is the schema name, table name or column name incorrect?';
			RETURN FALSE;
		END IF;

		RETURN TRUE;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.addrasterconstraints(rasttable name, rastcolumn name, srid boolean DEFAULT true, scale_x boolean DEFAULT true, scale_y boolean DEFAULT true, blocksize_x boolean DEFAULT true, blocksize_y boolean DEFAULT true, same_alignment boolean DEFAULT true, regular_blocking boolean DEFAULT false, num_bands boolean DEFAULT true, pixel_types boolean DEFAULT true, nodata_values boolean DEFAULT true, out_db boolean DEFAULT true, extent boolean DEFAULT true)
 RETURNS boolean
 LANGUAGE sql
 STRICT
AS $function$ SELECT public.AddRasterConstraints('', $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.addrasterconstraints(rastschema name, rasttable name, rastcolumn name, VARIADIC constraints text[])
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		max int;
		cnt int;
		sql text;
		schema name;
		x int;
		kw text;
		rtn boolean;
	BEGIN
		cnt := 0;
		max := array_length(constraints, 1);
		IF max < 1 THEN
			RAISE NOTICE 'No constraints indicated to be added.  Doing nothing';
			RETURN TRUE;
		END IF;

		-- validate schema
		schema := NULL;
		IF length($1) > 0 THEN
			sql := 'SELECT nspname FROM pg_namespace '
				|| 'WHERE nspname = ' || quote_literal($1)
				|| 'LIMIT 1';
			EXECUTE sql INTO schema;

			IF schema IS NULL THEN
				RAISE EXCEPTION 'The value provided for schema is invalid';
				RETURN FALSE;
			END IF;
		END IF;

		IF schema IS NULL THEN
			sql := 'SELECT n.nspname AS schemaname '
				|| 'FROM pg_catalog.pg_class c '
				|| 'JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace '
				|| 'WHERE c.relkind = ' || quote_literal('r')
				|| ' AND n.nspname NOT IN (' || quote_literal('pg_catalog')
				|| ', ' || quote_literal('pg_toast')
				|| ') AND pg_catalog.pg_table_is_visible(c.oid)'
				|| ' AND c.relname = ' || quote_literal($2);
			EXECUTE sql INTO schema;

			IF schema IS NULL THEN
				RAISE EXCEPTION 'The table % does not occur in the search_path', quote_literal($2);
				RETURN FALSE;
			END IF;
		END IF;

		<<kwloop>>
		FOR x in 1..max LOOP
			kw := trim(both from lower(constraints[x]));

			BEGIN
				CASE
					WHEN kw = 'srid' THEN
						RAISE NOTICE 'Adding SRID constraint';
						rtn :=  public._add_raster_constraint_srid(schema, $2, $3);
					WHEN kw IN ('scale_x', 'scalex') THEN
						RAISE NOTICE 'Adding scale-X constraint';
						rtn :=  public._add_raster_constraint_scale(schema, $2, $3, 'x');
					WHEN kw IN ('scale_y', 'scaley') THEN
						RAISE NOTICE 'Adding scale-Y constraint';
						rtn :=  public._add_raster_constraint_scale(schema, $2, $3, 'y');
					WHEN kw = 'scale' THEN
						RAISE NOTICE 'Adding scale-X constraint';
						rtn :=  public._add_raster_constraint_scale(schema, $2, $3, 'x');
						RAISE NOTICE 'Adding scale-Y constraint';
						rtn :=  public._add_raster_constraint_scale(schema, $2, $3, 'y');
					WHEN kw IN ('blocksize_x', 'blocksizex', 'width') THEN
						RAISE NOTICE 'Adding blocksize-X constraint';
						rtn :=  public._add_raster_constraint_blocksize(schema, $2, $3, 'width');
					WHEN kw IN ('blocksize_y', 'blocksizey', 'height') THEN
						RAISE NOTICE 'Adding blocksize-Y constraint';
						rtn :=  public._add_raster_constraint_blocksize(schema, $2, $3, 'height');
					WHEN kw = 'blocksize' THEN
						RAISE NOTICE 'Adding blocksize-X constraint';
						rtn :=  public._add_raster_constraint_blocksize(schema, $2, $3, 'width');
						RAISE NOTICE 'Adding blocksize-Y constraint';
						rtn :=  public._add_raster_constraint_blocksize(schema, $2, $3, 'height');
					WHEN kw IN ('same_alignment', 'samealignment', 'alignment') THEN
						RAISE NOTICE 'Adding alignment constraint';
						rtn :=  public._add_raster_constraint_alignment(schema, $2, $3);
					WHEN kw IN ('regular_blocking', 'regularblocking') THEN
						RAISE NOTICE 'Adding coverage tile constraint required for regular blocking';
						rtn :=  public._add_raster_constraint_coverage_tile(schema, $2, $3);
						IF rtn IS NOT FALSE THEN
							RAISE NOTICE 'Adding spatially unique constraint required for regular blocking';
							rtn :=  public._add_raster_constraint_spatially_unique(schema, $2, $3);
						END IF;
					WHEN kw IN ('num_bands', 'numbands') THEN
						RAISE NOTICE 'Adding number of bands constraint';
						rtn :=  public._add_raster_constraint_num_bands(schema, $2, $3);
					WHEN kw IN ('pixel_types', 'pixeltypes') THEN
						RAISE NOTICE 'Adding pixel type constraint';
						rtn :=  public._add_raster_constraint_pixel_types(schema, $2, $3);
					WHEN kw IN ('nodata_values', 'nodatavalues', 'nodata') THEN
						RAISE NOTICE 'Adding nodata value constraint';
						rtn :=  public._add_raster_constraint_nodata_values(schema, $2, $3);
					WHEN kw IN ('out_db', 'outdb') THEN
						RAISE NOTICE 'Adding out-of-database constraint';
						rtn :=  public._add_raster_constraint_out_db(schema, $2, $3);
					WHEN kw = 'extent' THEN
						RAISE NOTICE 'Adding maximum extent constraint';
						rtn :=  public._add_raster_constraint_extent(schema, $2, $3);
					ELSE
						RAISE NOTICE 'Unknown constraint: %.  Skipping', quote_literal(constraints[x]);
						CONTINUE kwloop;
				END CASE;
			END;

			IF rtn IS FALSE THEN
				cnt := cnt + 1;
				RAISE WARNING 'Unable to add constraint: %.  Skipping', quote_literal(constraints[x]);
			END IF;

		END LOOP kwloop;

		IF cnt = max THEN
			RAISE EXCEPTION 'None of the constraints specified could be added.  Is the schema name, table name or column name incorrect?';
			RETURN FALSE;
		END IF;

		RETURN TRUE;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.addrasterconstraints(rasttable name, rastcolumn name, VARIADIC constraints text[])
 RETURNS boolean
 LANGUAGE sql
 STRICT
AS $function$ SELECT public.AddRasterConstraints('', $1, $2, VARIADIC $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.addrasterconstraints(rastschema name, rasttable name, rastcolumn name, srid boolean DEFAULT true, scale_x boolean DEFAULT true, scale_y boolean DEFAULT true, blocksize_x boolean DEFAULT true, blocksize_y boolean DEFAULT true, same_alignment boolean DEFAULT true, regular_blocking boolean DEFAULT false, num_bands boolean DEFAULT true, pixel_types boolean DEFAULT true, nodata_values boolean DEFAULT true, out_db boolean DEFAULT true, extent boolean DEFAULT true)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		constraints text[];
	BEGIN
		IF srid IS TRUE THEN
			constraints := constraints || 'srid'::text;
		END IF;

		IF scale_x IS TRUE THEN
			constraints := constraints || 'scale_x'::text;
		END IF;

		IF scale_y IS TRUE THEN
			constraints := constraints || 'scale_y'::text;
		END IF;

		IF blocksize_x IS TRUE THEN
			constraints := constraints || 'blocksize_x'::text;
		END IF;

		IF blocksize_y IS TRUE THEN
			constraints := constraints || 'blocksize_y'::text;
		END IF;

		IF same_alignment IS TRUE THEN
			constraints := constraints || 'same_alignment'::text;
		END IF;

		IF regular_blocking IS TRUE THEN
			constraints := constraints || 'regular_blocking'::text;
		END IF;

		IF num_bands IS TRUE THEN
			constraints := constraints || 'num_bands'::text;
		END IF;

		IF pixel_types IS TRUE THEN
			constraints := constraints || 'pixel_types'::text;
		END IF;

		IF nodata_values IS TRUE THEN
			constraints := constraints || 'nodata_values'::text;
		END IF;

		IF out_db IS TRUE THEN
			constraints := constraints || 'out_db'::text;
		END IF;

		IF extent IS TRUE THEN
			constraints := constraints || 'extent'::text;
		END IF;

		RETURN public.AddRasterConstraints($1, $2, $3, VARIADIC constraints);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.armor(bytea, text[], text[])
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pg_armor$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.armor(bytea)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pg_armor$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.box(geometry)
 RETURNS box
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$LWGEOM_to_BOX$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.box(box3d)
 RETURNS box
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX3D_to_BOX$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.box2d(geometry)
 RETURNS box2d
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$LWGEOM_to_BOX2D$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.box2d(box3d)
 RETURNS box2d
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX3D_to_BOX2D$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.box2d_in(cstring)
 RETURNS box2d
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX2D_in$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.box2d_out(box2d)
 RETURNS cstring
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX2D_out$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.box2df_in(cstring)
 RETURNS box2df
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$box2df_in$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.box2df_out(box2df)
 RETURNS cstring
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$box2df_out$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.box3d(raster)
 RETURNS box3d
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select box3d( public.ST_convexhull($1))$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.box3d(box2d)
 RETURNS box3d
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX2D_to_BOX3D$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.box3d(geometry)
 RETURNS box3d
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$LWGEOM_to_BOX3D$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.box3d_in(cstring)
 RETURNS box3d
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX3D_in$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.box3d_out(box3d)
 RETURNS cstring
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX3D_out$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.box3dtobox(box3d)
 RETURNS box
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX3D_to_BOX$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.bytea(geometry)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$LWGEOM_to_bytea$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.bytea(geography)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_to_bytea$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.bytea(raster)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_to_bytea$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.checkauth(text, text)
 RETURNS integer
 LANGUAGE sql
AS $function$ SELECT CheckAuth('', $1, $2) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.checkauth(text, text, text)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
DECLARE
	schema text;
BEGIN
	IF NOT LongTransactionsEnabled() THEN
		RAISE EXCEPTION 'Long transaction support disabled, use EnableLongTransaction() to enable.';
	END IF;

	if ( $1 != '' ) THEN
		schema = $1;
	ELSE
		SELECT current_schema() into schema;
	END IF;

	-- TODO: check for an already existing trigger ?

	EXECUTE 'CREATE TRIGGER check_auth BEFORE UPDATE OR DELETE ON '
		|| quote_ident(schema) || '.' || quote_ident($2)
		||' FOR EACH ROW EXECUTE PROCEDURE CheckAuthTrigger('
		|| quote_literal($3) || ')';

	RETURN 0;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.checkauthtrigger()
 RETURNS trigger
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$check_authorization$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.cnf_injectstaticnovdata(targetmunicode integer)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
  	DECLARE
  		nov_rec RECORD;
  		pers_rec RECORD;
  		fullname TEXT;
  		fixedname TEXT;
  		fixedaddr TEXT;
  		fixedcity TEXT;
  		fixedstate TEXT;
  		fixedzip TEXT;
  		nov_count INTEGER;
	BEGIN
		nov_count := 0;
		FOR nov_rec IN SELECT noticeid, personid_recipient FROM public.noticeofviolation 
			INNER JOIN public.cecase ON (noticeofviolation.caseid = cecase.caseid)
			INNER JOIN public.property ON (cecase.property_propertyid = property.propertyid)
			WHERE municipality_municode = targetmunicode

			LOOP -- over NOVs by MUNI
				SELECT personid, fname, lname, address_street, address_city, 
       				address_state, address_zip FROM public.person WHERE personid = nov_rec.personid_recipient INTO pers_rec;

   				RAISE NOTICE 'WRITING FIXED RECIPIENT ID % INTO NOV ID %', nov_rec.personid_recipient, nov_rec.noticeid;
   				fullname := pers_rec.fname || ' ' || pers_rec.lname;

   				EXECUTE format('UPDATE noticeofviolation SET 
   					fixedrecipientxferts = now(), 
   					fixedrecipientname = %L,
   					fixedrecipientstreet = %L,
				    fixedrecipientcity = %L,
				    fixedrecipientstate = %L,
				    fixedrecipientzip = %L WHERE noticeid = %L;',
				    fullname,
				    pers_rec.address_street,
				    pers_rec.address_city,
				    pers_rec.address_state,
				    pers_rec.address_zip,
				    nov_rec.noticeid);
   				nov_count := nov_count + 1;
   				RAISE NOTICE 'UPDATE SUCCESS! Count: % ', nov_count;
			END LOOP; -- loop over NOVs by MUNI
		RETURN nov_count;
	END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.cnf_parsezipcode(zipraw text)
 RETURNS text
 LANGUAGE plpgsql
AS $function$
  	DECLARE
  		cleanzip TEXT;

	BEGIN
		cleanzip := substring(zipraw FROM '(\d{5})');
		IF cleanzip IS NOT NULL
			THEN
				RETURN cleanzip;
			ELSE
				RETURN '';
		END IF;
	END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.contains_2d(box2df, geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_contains_box2df_geom_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.contains_2d(geometry, box2df)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT $2 OPERATOR(public.@) $1;$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.contains_2d(box2df, box2df)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_contains_box2df_box2df_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.copycleartextpswds()
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
DECLARE
 userrow RECORD;
BEGIN
 RAISE NOTICE 'starting transfer...';
 FOR userrow IN SELECT password, userid
 FROM login LOOP
	EXECUTE format('UPDATE login SET pswdcleartext = %L WHERE userid = %L ', userrow.password, userrow.userid); 
	END LOOP;
RETURN 1;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.createghostperson(person_row person, userid integer)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$

DECLARE
	newpersonid integer;

BEGIN
       

	INSERT INTO public.person(
		    personid, persontype, muni_municode, fname, lname, jobtitle, 
		    phonecell, phonehome, phonework, email, address_street, address_city, 
		    address_state, address_zip, notes, lastupdated, expirydate, isactive, 
		    isunder18, humanverifiedby, compositelname, sourceid, creator, 
		    businessentity, mailing_address_street, mailing_address_city, 
		    mailing_address_zip, mailing_address_state, useseparatemailingaddr, 
		    expirynotes, creationtimestamp, canexpire, userlink, mailing_address_thirdline, 
            ghostof, ghostby, ghosttimestamp, cloneof, clonedby, clonetimestamp, 
            referenceperson)
	    VALUES (DEFAULT, person_row.persontype, person_row.muni_municode, person_row.fname, person_row.lname, person_row.jobtitle, 
		    person_row.phonecell, person_row.phonehome, person_row.phonework, person_row.email, person_row.address_street, person_row.address_city, 
		    person_row.address_state, person_row.address_zip, person_row.notes, now(), NULL, TRUE, 
		    person_row.isunder18, NULL, person_row.compositelname, person_row.sourceid , person_row.creator, 
		    person_row.businessentity, person_row.mailing_address_street, person_row.mailing_address_city, 
		    person_row.mailing_address_zip, person_row.mailing_address_state, person_row.useseparatemailingaddr, 
		    person_row.expirynotes, person_row.creationtimestamp, person_row.canexpire, person_row.userlink, person_row.mailing_address_thirdline,
		    person_row.personid, userid, now(), NULL, NULL, NULL,
		    NULL);

	    newpersonid :=currval('person_personidseq');

	    RETURN newpersonid;

END;

$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.crypt(text, text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pg_crypt$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.dearmor(text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pg_dearmor$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.decrypt(bytea, bytea, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pg_decrypt$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.decrypt_iv(bytea, bytea, bytea, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pg_decrypt_iv$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.difference(text, text)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/fuzzystrmatch', $function$difference$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.digest(text, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pg_digest$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.digest(bytea, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pg_digest$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.disablelongtransactions()
 RETURNS text
 LANGUAGE plpgsql
AS $function$
DECLARE
	rec RECORD;

BEGIN

	--
	-- Drop all triggers applied by CheckAuth()
	--
	FOR rec IN
		SELECT c.relname, t.tgname, t.tgargs FROM pg_trigger t, pg_class c, pg_proc p
		WHERE p.proname = 'checkauthtrigger' and t.tgfoid = p.oid and t.tgrelid = c.oid
	LOOP
		EXECUTE 'DROP TRIGGER ' || quote_ident(rec.tgname) ||
			' ON ' || quote_ident(rec.relname);
	END LOOP;

	--
	-- Drop the authorization_table table
	--
	FOR rec IN SELECT * FROM pg_class WHERE relname = 'authorization_table' LOOP
		DROP TABLE authorization_table;
	END LOOP;

	--
	-- Drop the authorized_tables view
	--
	FOR rec IN SELECT * FROM pg_class WHERE relname = 'authorized_tables' LOOP
		DROP VIEW authorized_tables;
	END LOOP;

	RETURN 'Long transactions support disabled';
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.dmetaphone(text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/fuzzystrmatch', $function$dmetaphone$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.dmetaphone_alt(text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/fuzzystrmatch', $function$dmetaphone_alt$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.dropgeometrycolumn(table_name character varying, column_name character varying)
 RETURNS text
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
	ret text;
BEGIN
	SELECT public.DropGeometryColumn('','',$1,$2) into ret;
	RETURN ret;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.dropgeometrycolumn(catalog_name character varying, schema_name character varying, table_name character varying, column_name character varying)
 RETURNS text
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
	myrec RECORD;
	okay boolean;
	real_schema name;

BEGIN

	-- Find, check or fix schema_name
	IF ( schema_name != '' ) THEN
		okay = false;

		FOR myrec IN SELECT nspname FROM pg_namespace WHERE text(nspname) = schema_name LOOP
			okay := true;
		END LOOP;

		IF ( okay <>  true ) THEN
			RAISE NOTICE 'Invalid schema name - using current_schema()';
			SELECT current_schema() into real_schema;
		ELSE
			real_schema = schema_name;
		END IF;
	ELSE
		SELECT current_schema() into real_schema;
	END IF;

	-- Find out if the column is in the geometry_columns table
	okay = false;
	FOR myrec IN SELECT * from public.geometry_columns where f_table_schema = text(real_schema) and f_table_name = table_name and f_geometry_column = column_name LOOP
		okay := true;
	END LOOP;
	IF (okay <> true) THEN
		RAISE EXCEPTION 'column not found in geometry_columns table';
		RETURN false;
	END IF;

	-- Remove table column
	EXECUTE 'ALTER TABLE ' || quote_ident(real_schema) || '.' ||
		quote_ident(table_name) || ' DROP COLUMN ' ||
		quote_ident(column_name);

	RETURN real_schema || '.' || table_name || '.' || column_name ||' effectively removed.';

END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.dropgeometrycolumn(schema_name character varying, table_name character varying, column_name character varying)
 RETURNS text
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
	ret text;
BEGIN
	SELECT public.DropGeometryColumn('',$1,$2,$3) into ret;
	RETURN ret;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.dropgeometrytable(table_name character varying)
 RETURNS text
 LANGUAGE sql
 STRICT
AS $function$ SELECT public.DropGeometryTable('','',$1) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.dropgeometrytable(schema_name character varying, table_name character varying)
 RETURNS text
 LANGUAGE sql
 STRICT
AS $function$ SELECT public.DropGeometryTable('',$1,$2) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.dropgeometrytable(catalog_name character varying, schema_name character varying, table_name character varying)
 RETURNS text
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
	real_schema name;

BEGIN

	IF ( schema_name = '' ) THEN
		SELECT current_schema() into real_schema;
	ELSE
		real_schema = schema_name;
	END IF;

	-- TODO: Should we warn if table doesn't exist probably instead just saying dropped
	-- Remove table
	EXECUTE 'DROP TABLE IF EXISTS '
		|| quote_ident(real_schema) || '.' ||
		quote_ident(table_name) || ' RESTRICT';

	RETURN
		real_schema || '.' ||
		table_name ||' dropped.';

END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.dropoverviewconstraints(ovschema name, ovtable name, ovcolumn name)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		schema name;
		sql text;
		rtn boolean;
	BEGIN
		-- validate schema
		schema := NULL;
		IF length($1) > 0 THEN
			sql := 'SELECT nspname FROM pg_namespace '
				|| 'WHERE nspname = ' || quote_literal($1)
				|| 'LIMIT 1';
			EXECUTE sql INTO schema;

			IF schema IS NULL THEN
				RAISE EXCEPTION 'The value provided for schema is invalid';
				RETURN FALSE;
			END IF;
		END IF;

		IF schema IS NULL THEN
			sql := 'SELECT n.nspname AS schemaname '
				|| 'FROM pg_catalog.pg_class c '
				|| 'JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace '
				|| 'WHERE c.relkind = ' || quote_literal('r')
				|| ' AND n.nspname NOT IN (' || quote_literal('pg_catalog')
				|| ', ' || quote_literal('pg_toast')
				|| ') AND pg_catalog.pg_table_is_visible(c.oid)'
				|| ' AND c.relname = ' || quote_literal($2);
			EXECUTE sql INTO schema;

			IF schema IS NULL THEN
				RAISE EXCEPTION 'The table % does not occur in the search_path', quote_literal($2);
				RETURN FALSE;
			END IF;
		END IF;

		rtn :=  public._drop_overview_constraint(schema, $2, $3);
		IF rtn IS FALSE THEN
			RAISE EXCEPTION 'Unable to drop the overview constraint .  Is the schema name, table name or column name incorrect?';
			RETURN FALSE;
		END IF;

		RETURN TRUE;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.dropoverviewconstraints(ovtable name, ovcolumn name)
 RETURNS boolean
 LANGUAGE sql
 STRICT
AS $function$ SELECT  public.DropOverviewConstraints('', $1, $2) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.droprasterconstraints(rastschema name, rasttable name, rastcolumn name, srid boolean DEFAULT true, scale_x boolean DEFAULT true, scale_y boolean DEFAULT true, blocksize_x boolean DEFAULT true, blocksize_y boolean DEFAULT true, same_alignment boolean DEFAULT true, regular_blocking boolean DEFAULT true, num_bands boolean DEFAULT true, pixel_types boolean DEFAULT true, nodata_values boolean DEFAULT true, out_db boolean DEFAULT true, extent boolean DEFAULT true)
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		constraints text[];
	BEGIN
		IF srid IS TRUE THEN
			constraints := constraints || 'srid'::text;
		END IF;

		IF scale_x IS TRUE THEN
			constraints := constraints || 'scale_x'::text;
		END IF;

		IF scale_y IS TRUE THEN
			constraints := constraints || 'scale_y'::text;
		END IF;

		IF blocksize_x IS TRUE THEN
			constraints := constraints || 'blocksize_x'::text;
		END IF;

		IF blocksize_y IS TRUE THEN
			constraints := constraints || 'blocksize_y'::text;
		END IF;

		IF same_alignment IS TRUE THEN
			constraints := constraints || 'same_alignment'::text;
		END IF;

		IF regular_blocking IS TRUE THEN
			constraints := constraints || 'regular_blocking'::text;
		END IF;

		IF num_bands IS TRUE THEN
			constraints := constraints || 'num_bands'::text;
		END IF;

		IF pixel_types IS TRUE THEN
			constraints := constraints || 'pixel_types'::text;
		END IF;

		IF nodata_values IS TRUE THEN
			constraints := constraints || 'nodata_values'::text;
		END IF;

		IF out_db IS TRUE THEN
			constraints := constraints || 'out_db'::text;
		END IF;

		IF extent IS TRUE THEN
			constraints := constraints || 'extent'::text;
		END IF;

		RETURN DropRasterConstraints($1, $2, $3, VARIADIC constraints);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.droprasterconstraints(rasttable name, rastcolumn name, VARIADIC constraints text[])
 RETURNS boolean
 LANGUAGE sql
 STRICT
AS $function$ SELECT  public.DropRasterConstraints('', $1, $2, VARIADIC $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.droprasterconstraints(rastschema name, rasttable name, rastcolumn name, VARIADIC constraints text[])
 RETURNS boolean
 LANGUAGE plpgsql
 STRICT
AS $function$
	DECLARE
		max int;
		x int;
		schema name;
		sql text;
		kw text;
		rtn boolean;
		cnt int;
	BEGIN
		cnt := 0;
		max := array_length(constraints, 1);
		IF max < 1 THEN
			RAISE NOTICE 'No constraints indicated to be dropped.  Doing nothing';
			RETURN TRUE;
		END IF;

		-- validate schema
		schema := NULL;
		IF length($1) > 0 THEN
			sql := 'SELECT nspname FROM pg_namespace '
				|| 'WHERE nspname = ' || quote_literal($1)
				|| 'LIMIT 1';
			EXECUTE sql INTO schema;

			IF schema IS NULL THEN
				RAISE EXCEPTION 'The value provided for schema is invalid';
				RETURN FALSE;
			END IF;
		END IF;

		IF schema IS NULL THEN
			sql := 'SELECT n.nspname AS schemaname '
				|| 'FROM pg_catalog.pg_class c '
				|| 'JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace '
				|| 'WHERE c.relkind = ' || quote_literal('r')
				|| ' AND n.nspname NOT IN (' || quote_literal('pg_catalog')
				|| ', ' || quote_literal('pg_toast')
				|| ') AND pg_catalog.pg_table_is_visible(c.oid)'
				|| ' AND c.relname = ' || quote_literal($2);
			EXECUTE sql INTO schema;

			IF schema IS NULL THEN
				RAISE EXCEPTION 'The table % does not occur in the search_path', quote_literal($2);
				RETURN FALSE;
			END IF;
		END IF;

		<<kwloop>>
		FOR x in 1..max LOOP
			kw := trim(both from lower(constraints[x]));

			BEGIN
				CASE
					WHEN kw = 'srid' THEN
						RAISE NOTICE 'Dropping SRID constraint';
						rtn :=  public._drop_raster_constraint_srid(schema, $2, $3);
					WHEN kw IN ('scale_x', 'scalex') THEN
						RAISE NOTICE 'Dropping scale-X constraint';
						rtn :=  public._drop_raster_constraint_scale(schema, $2, $3, 'x');
					WHEN kw IN ('scale_y', 'scaley') THEN
						RAISE NOTICE 'Dropping scale-Y constraint';
						rtn :=  public._drop_raster_constraint_scale(schema, $2, $3, 'y');
					WHEN kw = 'scale' THEN
						RAISE NOTICE 'Dropping scale-X constraint';
						rtn :=  public._drop_raster_constraint_scale(schema, $2, $3, 'x');
						RAISE NOTICE 'Dropping scale-Y constraint';
						rtn :=  public._drop_raster_constraint_scale(schema, $2, $3, 'y');
					WHEN kw IN ('blocksize_x', 'blocksizex', 'width') THEN
						RAISE NOTICE 'Dropping blocksize-X constraint';
						rtn :=  public._drop_raster_constraint_blocksize(schema, $2, $3, 'width');
					WHEN kw IN ('blocksize_y', 'blocksizey', 'height') THEN
						RAISE NOTICE 'Dropping blocksize-Y constraint';
						rtn :=  public._drop_raster_constraint_blocksize(schema, $2, $3, 'height');
					WHEN kw = 'blocksize' THEN
						RAISE NOTICE 'Dropping blocksize-X constraint';
						rtn :=  public._drop_raster_constraint_blocksize(schema, $2, $3, 'width');
						RAISE NOTICE 'Dropping blocksize-Y constraint';
						rtn :=  public._drop_raster_constraint_blocksize(schema, $2, $3, 'height');
					WHEN kw IN ('same_alignment', 'samealignment', 'alignment') THEN
						RAISE NOTICE 'Dropping alignment constraint';
						rtn :=  public._drop_raster_constraint_alignment(schema, $2, $3);
					WHEN kw IN ('regular_blocking', 'regularblocking') THEN
						rtn :=  public._drop_raster_constraint_regular_blocking(schema, $2, $3);

						RAISE NOTICE 'Dropping coverage tile constraint required for regular blocking';
						rtn :=  public._drop_raster_constraint_coverage_tile(schema, $2, $3);

						IF rtn IS NOT FALSE THEN
							RAISE NOTICE 'Dropping spatially unique constraint required for regular blocking';
							rtn :=  public._drop_raster_constraint_spatially_unique(schema, $2, $3);
						END IF;
					WHEN kw IN ('num_bands', 'numbands') THEN
						RAISE NOTICE 'Dropping number of bands constraint';
						rtn :=  public._drop_raster_constraint_num_bands(schema, $2, $3);
					WHEN kw IN ('pixel_types', 'pixeltypes') THEN
						RAISE NOTICE 'Dropping pixel type constraint';
						rtn :=  public._drop_raster_constraint_pixel_types(schema, $2, $3);
					WHEN kw IN ('nodata_values', 'nodatavalues', 'nodata') THEN
						RAISE NOTICE 'Dropping nodata value constraint';
						rtn :=  public._drop_raster_constraint_nodata_values(schema, $2, $3);
					WHEN kw IN ('out_db', 'outdb') THEN
						RAISE NOTICE 'Dropping out-of-database constraint';
						rtn :=  public._drop_raster_constraint_out_db(schema, $2, $3);
					WHEN kw = 'extent' THEN
						RAISE NOTICE 'Dropping maximum extent constraint';
						rtn :=  public._drop_raster_constraint_extent(schema, $2, $3);
					ELSE
						RAISE NOTICE 'Unknown constraint: %.  Skipping', quote_literal(constraints[x]);
						CONTINUE kwloop;
				END CASE;
			END;

			IF rtn IS FALSE THEN
				cnt := cnt + 1;
				RAISE WARNING 'Unable to drop constraint: %.  Skipping', quote_literal(constraints[x]);
			END IF;

		END LOOP kwloop;

		IF cnt = max THEN
			RAISE EXCEPTION 'None of the constraints specified could be dropped.  Is the schema name, table name or column name incorrect?';
			RETURN FALSE;
		END IF;

		RETURN TRUE;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.droprasterconstraints(rasttable name, rastcolumn name, srid boolean DEFAULT true, scale_x boolean DEFAULT true, scale_y boolean DEFAULT true, blocksize_x boolean DEFAULT true, blocksize_y boolean DEFAULT true, same_alignment boolean DEFAULT true, regular_blocking boolean DEFAULT true, num_bands boolean DEFAULT true, pixel_types boolean DEFAULT true, nodata_values boolean DEFAULT true, out_db boolean DEFAULT true, extent boolean DEFAULT true)
 RETURNS boolean
 LANGUAGE sql
 STRICT
AS $function$ SELECT DropRasterConstraints('', $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.enablelongtransactions()
 RETURNS text
 LANGUAGE plpgsql
AS $function$
DECLARE
	"query" text;
	exists bool;
	rec RECORD;

BEGIN

	exists = 'f';
	FOR rec IN SELECT * FROM pg_class WHERE relname = 'authorization_table'
	LOOP
		exists = 't';
	END LOOP;

	IF NOT exists
	THEN
		"query" = 'CREATE TABLE authorization_table (
			toid oid, -- table oid
			rid text, -- row id
			expires timestamp,
			authid text
		)';
		EXECUTE "query";
	END IF;

	exists = 'f';
	FOR rec IN SELECT * FROM pg_class WHERE relname = 'authorized_tables'
	LOOP
		exists = 't';
	END LOOP;

	IF NOT exists THEN
		"query" = 'CREATE VIEW authorized_tables AS ' ||
			'SELECT ' ||
			'n.nspname as schema, ' ||
			'c.relname as table, trim(' ||
			quote_literal(chr(92) || '000') ||
			' from t.tgargs) as id_column ' ||
			'FROM pg_trigger t, pg_class c, pg_proc p ' ||
			', pg_namespace n ' ||
			'WHERE p.proname = ' || quote_literal('checkauthtrigger') ||
			' AND c.relnamespace = n.oid' ||
			' AND t.tgfoid = p.oid and t.tgrelid = c.oid';
		EXECUTE "query";
	END IF;

	RETURN 'Long transactions support enabled';
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.encrypt(bytea, bytea, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pg_encrypt$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.encrypt_iv(bytea, bytea, bytea, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pg_encrypt_iv$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.equals(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_Equals$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.extractbuildingno(addr text)
 RETURNS text
 LANGUAGE plpgsql
AS $function$
	DECLARE
	 	extractedbldg TEXT;

	BEGIN
		IF addr ILIKE '%PO BOX%'
			THEN 
				extractedbldg := substring(addr from '[Pp][Oo]\s[Bb][Oo][Xx]\s\d+');
			ELSE
				IF addr ILIKE '%/%'
				THEN -- we've got a XX 1/2 street
					extractedbldg := substring(addr from '\d+\W\d/\d');
				ELSE 
					extractedbldg := substring(addr from '\d+');
				END IF; -- fractions
		END IF; -- PO boxes

		RETURN unifyspacesandtrim(extractedbldg);
	END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.extractstreet(addr text)
 RETURNS text
 LANGUAGE plpgsql
AS $function$
	DECLARE
	 	extractedstreet TEXT;
	 	re_matches RECORD;
	 	validationstring TEXT;

	BEGIN
		IF addr ILIKE '%PO BOX%'
			THEN
				RETURN 'PO BOX';
			ELSE
				IF addr ILIKE '%/%'
					THEN -- we've got a XX 1/2 street
						extractedstreet := substring(addr from '\d+\W\d/\d\W?(.*)');
					ELSE 
						IF addr ILIKE'%-%'
							THEN --we've got a range of some sort
								SELECT regexp_matches(addr, '\w-\w') INTO re_matches;
								IF NOT FOUND
									THEN -- we've got an address range and not a unit range
										extractedstreet := substring(addr from '\d+\W?-\d+\W(.*)');
									ELSE -- we've likely got a unit range, so skip
										RAISE NOTICE 'found unit range in address; skipping: %', addr;
										RETURN NULL;
								END IF;
							ELSE -- no address range
								extractedstreet := substring(addr from '\d+\W(.*)');
						END IF; -- range check
				END IF; -- fraction check
		END IF; -- box box
		validationstring := unifyspacesandtrim(extractedstreet);
		-- check work for null, and empty strings and single spaces
		IF validationstring IS NOT NULL AND validationstring <> '' AND validationstring <> ' '
			THEN
				RETURN validationstring;
			ELSE
				RETURN NULL;
		END IF; --validation
	END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.find_srid(character varying, character varying, character varying)
 RETURNS integer
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
DECLARE
	schem varchar =  $1;
	tabl varchar = $2;
	sr int4;
BEGIN
-- if the table contains a . and the schema is empty
-- split the table into a schema and a table
-- otherwise drop through to default behavior
	IF ( schem = '' and strpos(tabl,'.') > 0 ) THEN
	 schem = substr(tabl,1,strpos(tabl,'.')-1);
	 tabl = substr(tabl,length(schem)+2);
	END IF;

	select SRID into sr from public.geometry_columns where (f_table_schema = schem or schem = '') and f_table_name = tabl and f_geometry_column = $3;
	IF NOT FOUND THEN
	   RAISE EXCEPTION 'find_srid() - could not find the corresponding SRID - is the geometry registered in the GEOMETRY_COLUMNS table?  Is there an uppercase/lowercase mismatch?';
	END IF;
	return sr;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.gen_random_bytes(integer)
 RETURNS bytea
 LANGUAGE c
 STRICT
AS '$libdir/pgcrypto', $function$pg_random_bytes$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.gen_random_uuid()
 RETURNS uuid
 LANGUAGE c
AS '$libdir/pgcrypto', $function$pg_random_uuid$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.gen_salt(text)
 RETURNS text
 LANGUAGE c
 STRICT
AS '$libdir/pgcrypto', $function$pg_gen_salt$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.gen_salt(text, integer)
 RETURNS text
 LANGUAGE c
 STRICT
AS '$libdir/pgcrypto', $function$pg_gen_salt_rounds$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geog_brin_inclusion_add_value(internal, internal, internal, internal)
 RETURNS boolean
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$geog_brin_inclusion_add_value$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography(bytea)
 RETURNS geography
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_from_binary$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography(geography, integer, boolean)
 RETURNS geography
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_enforce_typmod$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography(geometry)
 RETURNS geography
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_from_geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_analyze(internal)
 RETURNS boolean
 LANGUAGE c
 STRICT
AS '$libdir/postgis-2.5', $function$gserialized_analyze_nd$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_cmp(geography, geography)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_cmp$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_distance_knn(geography, geography)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$geography_distance_knn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_eq(geography, geography)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_eq$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_ge(geography, geography)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_ge$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_gist_compress(internal)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_compress$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_gist_consistent(internal, geography, integer)
 RETURNS boolean
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_consistent$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_gist_decompress(internal)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_decompress$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_gist_distance(internal, geography, integer)
 RETURNS double precision
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_geog_distance$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_gist_penalty(internal, internal, internal)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_penalty$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_gist_picksplit(internal, internal)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_picksplit$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_gist_same(box2d, box2d, internal)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_same$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_gist_union(bytea, internal)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_union$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_gt(geography, geography)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_gt$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_in(cstring, oid, integer)
 RETURNS geography
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_in$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_le(geography, geography)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_le$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_lt(geography, geography)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_lt$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_out(geography)
 RETURNS cstring
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_out$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_overlaps(geography, geography)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_overlaps$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_recv(internal, oid, integer)
 RETURNS geography
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_recv$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_send(geography)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_send$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_typmod_in(cstring[])
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_typmod_in$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geography_typmod_out(integer)
 RETURNS cstring
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$postgis_typmod_out$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geom2d_brin_inclusion_add_value(internal, internal, internal, internal)
 RETURNS boolean
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$geom2d_brin_inclusion_add_value$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geom3d_brin_inclusion_add_value(internal, internal, internal, internal)
 RETURNS boolean
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$geom3d_brin_inclusion_add_value$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geom4d_brin_inclusion_add_value(internal, internal, internal, internal)
 RETURNS boolean
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$geom4d_brin_inclusion_add_value$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry(text)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$parse_WKT_lwgeom$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry(geography)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geometry_from_geography$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry(box2d)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX2D_to_LWGEOM$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry(geometry, integer, boolean)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geometry_enforce_typmod$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry(box3d)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX3D_to_LWGEOM$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry(polygon)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$polygon_to_geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry(bytea)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_from_bytea$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry(point)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$point_to_geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry(path)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$path_to_geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_above(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_above_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_analyze(internal)
 RETURNS boolean
 LANGUAGE c
 STRICT
AS '$libdir/postgis-2.5', $function$gserialized_analyze_nd$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_below(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_below_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_cmp(geom1 geometry, geom2 geometry)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$lwgeom_cmp$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_contained_by_raster(geometry, raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select $1 OPERATOR(public.@) $2::public.geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_contains(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_contains_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_distance_box(geom1 geometry, geom2 geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_distance_box_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_distance_centroid(geom1 geometry, geom2 geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$distance$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_distance_centroid_nd(geometry, geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_distance_nd$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_distance_cpa(geometry, geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_DistanceCPA$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_eq(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$lwgeom_eq$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_ge(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$lwgeom_ge$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_gist_compress_2d(internal)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_compress_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_gist_compress_nd(internal)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_compress$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_gist_consistent_2d(internal, geometry, integer)
 RETURNS boolean
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_consistent_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_gist_consistent_nd(internal, geometry, integer)
 RETURNS boolean
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_consistent$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_gist_decompress_2d(internal)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_decompress_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_gist_decompress_nd(internal)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_decompress$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_gist_distance_2d(internal, geometry, integer)
 RETURNS double precision
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_distance_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_gist_distance_nd(internal, geometry, integer)
 RETURNS double precision
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_distance$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_gist_penalty_2d(internal, internal, internal)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_penalty_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_gist_penalty_nd(internal, internal, internal)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_penalty$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_gist_picksplit_2d(internal, internal)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_picksplit_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_gist_picksplit_nd(internal, internal)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_picksplit$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_gist_same_2d(geom1 geometry, geom2 geometry, internal)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_same_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_gist_same_nd(geometry, geometry, internal)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_same$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_gist_union_2d(bytea, internal)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_union_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_gist_union_nd(bytea, internal)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_union$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_gt(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$lwgeom_gt$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_hash(geometry)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$lwgeom_hash$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_in(cstring)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_in$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_le(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$lwgeom_le$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_left(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_left_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_lt(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$lwgeom_lt$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_out(geometry)
 RETURNS cstring
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_out$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_overabove(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_overabove_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_overbelow(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_overbelow_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_overlaps(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_overlaps_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_overlaps_nd(geometry, geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_overlaps$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_overleft(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_overleft_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_overright(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_overright_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_raster_contain(geometry, raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select $1 OPERATOR(public.~) $2::public.geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_raster_overlap(geometry, raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select $1 OPERATOR(public.&&) $2::public.geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_recv(internal)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_recv$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_right(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_right_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_same(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_same_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_send(geometry)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_send$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_typmod_in(cstring[])
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geometry_typmod_in$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_typmod_out(integer)
 RETURNS cstring
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$postgis_typmod_out$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometry_within(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_within_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometrytype(geography)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_getTYPE$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geometrytype(geometry)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$LWGEOM_getTYPE$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geomfromewkb(bytea)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOMFromEWKB$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.geomfromewkt(text)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$parse_WKT_lwgeom$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.get_proj4_from_srid(integer)
 RETURNS text
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
BEGIN
	RETURN proj4text::text FROM public.spatial_ref_sys WHERE srid= $1;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.gettransactionid()
 RETURNS xid
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$getTransactionID$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.gidx_in(cstring)
 RETURNS gidx
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gidx_in$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.gidx_out(gidx)
 RETURNS cstring
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gidx_out$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.gserialized_gist_joinsel_2d(internal, oid, internal, smallint)
 RETURNS double precision
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_joinsel_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.gserialized_gist_joinsel_nd(internal, oid, internal, smallint)
 RETURNS double precision
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_joinsel_nd$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.gserialized_gist_sel_2d(internal, oid, internal, integer)
 RETURNS double precision
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_sel_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.gserialized_gist_sel_nd(internal, oid, internal, integer)
 RETURNS double precision
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$gserialized_gist_sel_nd$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.hashpasswords()
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
DECLARE
 userrow RECORD;
BEGIN
 RAISE NOTICE 'starting transfer...';
 FOR userrow IN SELECT pswdcleartext, userid
 FROM login LOOP
	EXECUTE format('UPDATE login SET password = encode(digest(%L, ''md5''), ''base64'') WHERE userid = %L ', userrow.pswdcleartext, userrow.userid); 
	END LOOP;
RETURN 1;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.hmac(text, text, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pg_hmac$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.hmac(bytea, bytea, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pg_hmac$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.is_contained_2d(box2df, box2df)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_contains_box2df_box2df_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.is_contained_2d(geometry, box2df)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT $2 OPERATOR(public.~) $1;$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.is_contained_2d(box2df, geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_within_box2df_geom_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.levenshtein(text, text, integer, integer, integer)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/fuzzystrmatch', $function$levenshtein_with_costs$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.levenshtein(text, text)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/fuzzystrmatch', $function$levenshtein$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.levenshtein_less_equal(text, text, integer)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/fuzzystrmatch', $function$levenshtein_less_equal$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.levenshtein_less_equal(text, text, integer, integer, integer, integer)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/fuzzystrmatch', $function$levenshtein_less_equal_with_costs$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.lockrow(text, text, text)
 RETURNS integer
 LANGUAGE sql
 STRICT
AS $function$ SELECT LockRow(current_schema(), $1, $2, $3, now()::timestamp+'1:00'); $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.lockrow(text, text, text, text, timestamp without time zone)
 RETURNS integer
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
	myschema alias for $1;
	mytable alias for $2;
	myrid   alias for $3;
	authid alias for $4;
	expires alias for $5;
	ret int;
	mytoid oid;
	myrec RECORD;

BEGIN

	IF NOT LongTransactionsEnabled() THEN
		RAISE EXCEPTION 'Long transaction support disabled, use EnableLongTransaction() to enable.';
	END IF;

	EXECUTE 'DELETE FROM authorization_table WHERE expires < now()';

	SELECT c.oid INTO mytoid FROM pg_class c, pg_namespace n
		WHERE c.relname = mytable
		AND c.relnamespace = n.oid
		AND n.nspname = myschema;

	-- RAISE NOTICE 'toid: %', mytoid;

	FOR myrec IN SELECT * FROM authorization_table WHERE
		toid = mytoid AND rid = myrid
	LOOP
		IF myrec.authid != authid THEN
			RETURN 0;
		ELSE
			RETURN 1;
		END IF;
	END LOOP;

	EXECUTE 'INSERT INTO authorization_table VALUES ('||
		quote_literal(mytoid::text)||','||quote_literal(myrid)||
		','||quote_literal(expires::text)||
		','||quote_literal(authid) ||')';

	GET DIAGNOSTICS ret = ROW_COUNT;

	RETURN ret;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.lockrow(text, text, text, text)
 RETURNS integer
 LANGUAGE sql
 STRICT
AS $function$ SELECT LockRow($1, $2, $3, $4, now()::timestamp+'1:00'); $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.lockrow(text, text, text, timestamp without time zone)
 RETURNS integer
 LANGUAGE sql
 STRICT
AS $function$ SELECT LockRow(current_schema(), $1, $2, $3, $4); $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.longtransactionsenabled()
 RETURNS boolean
 LANGUAGE plpgsql
AS $function$
DECLARE
	rec RECORD;
BEGIN
	FOR rec IN SELECT oid FROM pg_class WHERE relname = 'authorized_tables'
	LOOP
		return 't';
	END LOOP;
	return 'f';
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.metaphone(text, integer)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/fuzzystrmatch', $function$metaphone$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.migratepersontohuman(creationrobotuser integer, defaultsource integer, municodetarget integer, parcel_human_lorid integer, human_mailing_lorid integer)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
	DECLARE
	 	pr RECORD;
	 	prop_pers_rec RECORD;
	 	fullname TEXT;
	 	fresh_human_id INTEGER;
	 	current_human_id INTEGER;
	 	deacts TIMESTAMP WITH TIME ZONE;
	 	deacuser INTEGER;
	 	human_rec_count INTEGER;
	 	parcel_rec RECORD;
	 	mailing_rec RECORD;
	 	zip_parsed TEXT;
	 	citystatezip_rec RECORD;
	 	street_freshstreetname TEXT;
	 	street_freshid INTEGER;
	 	address_freshid INTEGER;
	 	pers_newaddr_street TEXT;
	 	pers_newaddr_bldgno TEXT;
	 	human_dupid INTEGER;

	BEGIN
		RAISE NOTICE '**** BEGIN PERSON TO HUMAN MIGRATION ****';

		human_rec_count := 0;

		FOR pr IN SELECT personid, persontype, muni_municode, fname, lname, jobtitle, 
		       phonecell, phonehome, phonework, email, address_street, address_city, 
		       address_state, address_zip, notes, lastupdated, expirydate, isactive, 
		       isunder18, humanverifiedby, compositelname, sourceid, creator, 
		       businessentity, mailing_address_street, mailing_address_city, 
		       mailing_address_zip, mailing_address_state, useseparatemailingaddr, 
		       expirynotes, creationtimestamp, canexpire, userlink, mailing_address_thirdline, 
		       ghostof, ghostby, ghosttimestamp, cloneof, clonedby, clonetimestamp, 
		       referenceperson, rawname, cleanname, multientity
		  			FROM public.person WHERE muni_municode = municodetarget
		 
			LOOP -- over legacy person records
				RAISE NOTICE 'ITERATION: personid: %, lname: %', pr.personid, pr.lname;
				
				IF (pr.lname IS NULL OR pr.lname = '') AND (pr.fname IS NULL OR pr.fname = '')
					THEN

						RAISE NOTICE 'found null or empty last AND first name; skipping person and LOGGING;';
						EXECUTE format ('INSERT INTO public.personhumanmigrationlog(
																            logentryid, human_humanid, person_personid, error_code, notes, ts)
																    VALUES (DEFAULT, NULL, %L, %L, ''EMTPY NAME'', now());',
																    pr.personid, 4 
																);
						CONTINUE;
				END IF;

				--concat name
				IF pr.fname IS NOT NULL
					THEN
						fullname := unifyspacesandtrim(pr.fname) || ' ' || unifyspacesandtrim(pr.lname); 
					ELSE 
						fullname := unifyspacesandtrim(pr.lname);
				END IF;

				RAISE NOTICE 'FULL name for personid % is %', pr.personid, fullname;

				-- Check for duplicate person in human table
				SELECT humanid INTO human_dupid FROM human WHERE unifyspacesandtrim(human.name) = fullname;

				RAISE NOTICE 'DUP CHECK: HUMAN_DUPID: % ', human_dupid;

				IF human_dupid IS NULL OR human_dupid = 0 -- no duplicate based on name only
					THEN  -- go ahead and write our new human records
						RAISE NOTICE 'NO DUP FOUND FOR %; writing new human', fullname;
						-- check for deactivation
						IF NOT pr.isactive 
							THEN 
								deacts := now();
								deacuser := 99; -- the cogbot
							ELSE
								deacts := NULL;
								deacuser := NULL;
						END IF;

						-- our new humanid is our old person id
						fresh_human_id := pr.personid;

						EXECUTE format('INSERT INTO public.human(
			            humanid, name, dob, under18, jobtitle, businessentity, multihuman, 
			            source_sourceid, deceaseddate, deceasedby_userid, cloneof_humanid, 
			            createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, 
			            deactivatedts, deactivatedby_userid, notes)
						    VALUES (%L, %L, NULL, %L, %L, FALSE, %L, 
						            %L, NULL, NULL, NULL, 
						            now(), %L, now(), %L, 
						            %L, %L, %L);', 
					               fresh_human_id, fullname, pr.isunder18, unifyspacesandtrim(pr.jobtitle), pr.multientity,
					               pr.sourceid, 
					               pr.creator, pr.creator, 
					               deacts, deacuser, unifyspacesandtrim(pr.notes)

			            ); 

			            RAISE NOTICE 'Fresh human record ID: %', fresh_human_id;
			            -- Now move our cursor to our new fresh human
			            current_human_id := fresh_human_id; -- NOTE if we have a dupe, current_human_id is not updated

			            human_rec_count := human_rec_count + 1;

						-- DEAL with "None" in phone columns, and empty strings

						IF pr.phonecell IS NOT NULL AND pr.phonecell <> '' AND pr.phonecell <> 'None'			
							THEN
								--RAISE NOTICE 'PHONE CELL FOUND: %', pr.phonecell;
								EXECUTE format('INSERT INTO public.contactphone(
											            phoneid, human_humanid, phonenumber, phoneext, phonetype_typeid, 
											            disconnectts, disconnect_userid, createdts, createdby_userid, 
											            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
											            notes)
											    VALUES (DEFAULT, %L, %L, NULL, 100, 
											            NULL, NULL, now(), %L, 
											            now(), %L, NULL, NULL, 
											            ''Created during person-human migration JUL-2021'');', 
											            		fresh_human_id, unifyspacesandtrim(pr.phonecell), 
											            					creationrobotuser,
								            					creationrobotuser);
							ELSE
								--RAISE NOTICE 'NO VALID PHONE CELL FOUND; NOT WRITING RECORD';
								NULL; -- don't write any phone records
						END IF;


						IF pr.phonehome IS NOT NULL AND pr.phonehome <> '' AND pr.phonehome <> 'None'			
							THEN
								--RAISE NOTICE 'PHONE HOME FOUND: %', pr.phonehome;
								EXECUTE format('INSERT INTO public.contactphone(
											            phoneid, human_humanid, phonenumber, phoneext, phonetype_typeid, 
											            disconnectts, disconnect_userid, createdts, createdby_userid, 
											            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
											            notes)
											    VALUES (DEFAULT, %L, %L, NULL, 101, 
											            NULL, NULL, now(), %L, 
											            now(), %L, NULL, NULL, 
											            ''Created during person-human migration JUL-2021'');', 
											            		fresh_human_id, unifyspacesandtrim(pr.phonehome), 
											            					creationrobotuser,
								            					creationrobotuser);
							ELSE -- duplicate
								--RAISE NOTICE 'NO VALID PHONE HOME FOUND; NOT WRITING RECORD';
								NULL; -- don't write any records
						END IF;


						IF pr.phonework IS NOT NULL AND pr.phonework <> '' AND pr.phonework <> 'None'			
							THEN
								--RAISE NOTICE 'PHONE WORK FOUND: %', pr.phonework;
								EXECUTE format('INSERT INTO public.contactphone(
											            phoneid, human_humanid, phonenumber, phoneext, phonetype_typeid, 
											            disconnectts, disconnect_userid, createdts, createdby_userid, 
											            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
											            notes)
											    VALUES (DEFAULT, %L, %L, NULL, 102, 
											            NULL, NULL, now(), %L, 
											            now(), %L, NULL, NULL, 
											            ''Created during person-human migration JUL-2021'');', 
											            		fresh_human_id, unifyspacesandtrim(pr.phonework), 
											            					creationrobotuser,
								            					creationrobotuser);
							ELSE
								--RAISE NOTICE 'NO VALID PHONE WORK FOUND; NOT WRITING RECORD';
								NULL; -- don't write any records
						END IF;
						
						IF pr.email IS NOT NULL AND pr.email <> '' AND pr.email <> 'None'			
						
							THEN
								--RAISE NOTICE 'EMAIL: WRITING RECORD % ', pr.email;
								EXECUTE format('INSERT INTO public.contactemail(
									            emailid, human_humanid, emailaddress, bouncets, createdts, createdby_userid, 
									            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
									            notes)
									    VALUES (DEFAULT, %L, %L, NULL, now(), %L, 
									            now(), %L, NULL, NULL, 
									            ''Created during person to human migration JUL-2021'');', 
							            		fresh_human_id, unifyspacesandtrim(pr.email), creationrobotuser,
				            					creationrobotuser);
							ELSE
								--RAISE NOTICE 'EMAIL: NO VALID EMAIL FOUND, SKIPPING';
								NULL; -- don't write any records
						END IF;

					ELSE  -- Record duplicate person found, don't write new humans, just write to log
						current_human_id := human_dupid;
						RAISE NOTICE 'DUPLICATE HUMAN FOUND: logging and using already inserted humanid % ', current_human_id  ;
						EXECUTE format('INSERT INTO public.personhumanmigrationlog(
									            logentryid, human_humanid, person_personid, error_code, notes, ts)
									    VALUES (DEFAULT, NULL, NULL, 2, %L, now());',
								    		 'DUPLICATE RECORD FOR fullname ' || fullname || ' With Person ID: ' || pr.personid
									    );

				END IF; -- over duplicate check of person records

				-- use existing property-person links to connect our new human to existing parcels
				FOR prop_pers_rec IN SELECT property_propertyid, person_personid, creationts
									 	 FROM public.propertyperson
									 	 WHERE person_personid = pr.personid
			 		LOOP -- begin iterating over property person records
			 			
			 			-- Link this human to an existing parcel

			 			SELECT parcelkey INTO parcel_rec FROM parcel WHERE parcelkey = prop_pers_rec.property_propertyid;
			 			IF FOUND
				 			THEN -- link human and parcel
				 			-- NOTE that we used the old property PK as the new parcel PK so this linking should be straightforward

				 				EXECUTE format('INSERT INTO public.humanparcel(
								                       linkid, human_humanid, parcel_parcelkey, source_sourceid, createdts, 
											            createdby_userid, lastupdatedts, lastupdatedby_userid, deactivatedts, 
											            deactivatedby_userid, notes, linkedobjectrole_lorid)
												    VALUES (DEFAULT, %L, %L, %L, now(), 
												    		%L, now(), %L, NULL,
												            NULL, ''Created during person-human migration AUG 2021'', %L);',
										            		current_human_id, parcel_rec.parcelkey, defaultsource, 
								            				creationrobotuser, creationrobotuser,
								            				parcel_human_lorid
					            				);
				 				RAISE NOTICE 'Linked parcel with PK % to Human with PK %', parcel_rec.parcelkey, current_human_id;

				 			-- If no parcel exists, write record to migrationlog table
				 			ELSE
				 				RAISE NOTICE 'No parcel found to link';
				 				-- EXECUTE format ('INSERT INTO public.personhumanmigrationlog(
									-- 				            logentryid, human_humanid, person_personid, error_code, notes, ts)
									-- 				    VALUES (DEFAULT, NULL, %L, %L, %L, now());',
									-- 				     pr.personid, 5, 
									-- 				    'UNABLE TO LINK PARCEL ' ||  parcel_rec.parcelkey || ' TO HUMAN ' || current_human_id
									-- 				);
		 				END IF;

			 			-- Next, check if this person's address is one of those linked any of the parcels to which he/she is connected, if so, do nothing
			 			-- but if their address is not associated with parcel in our current muni, then make a new record in the mailingaddress
			 			-- family and connect this fresh_human to a fresh address

			 			SELECT  addressid INTO mailing_rec
							FROM public.mailingaddress 
							INNER JOIN public.parcelmailingaddress 
								ON (mailingparcel_mailingid = addressid)
							WHERE mailingparcel_parcelid = parcel_rec.parcelkey
								AND bldgno ILIKE extractbuildingno(pr.address_street); -- NOTE: We're playing fast and loose
								-- with address matching: if the building numbers are the same, we assume it's the same address
								-- and don't write a new mailingaddress record. EDGE cases of folks having a separate mailing
								-- whose building numbers is exactly the same as their linked parcel are not addressed.
								-- THE PO box parsing process potentially makes this fraught so beware!

						IF FOUND
							THEN -- we've already got a link between the person and that person's address
								RAISE NOTICE 'Fresh human has legacy address already linked to a parcel: moving on';
								NULL;
							ELSE 	-- we don't have a mailing address that matches the person record's mailing address, 
									-- So write a new address and link it to our fresh human

								zip_parsed := cnf_parsezipcode(pr.address_zip);								
								SELECT id 
									FROM mailingcitystatezip 
									WHERE zip_code = zip_parsed AND list_type_id = 1 
									INTO citystatezip_rec;


								IF FOUND
									THEN --we've got a real zip code to attach to our new street
									RAISE NOTICE 'Fresh human has address with legitimite ZIP %', citystatezip_rec.id;
									street_freshstreetname := extractstreet(pr.address_street);
									RAISE NOTICE 'Extracted street from fresh address: %|<-SPACE CHECK', street_freshstreetname;

									IF street_freshstreetname IS NOT NULL
										THEN -- we have successfully extracted a street name

											SELECT streetid INTO street_freshid 
												FROM mailingstreet 
												WHERE citystatezip_cszipid = citystatezip_rec.id
													AND mailingstreet.name ILIKE street_freshstreetname;

											IF street_freshid IS NULL OR street_freshid = 0
												THEN -- no existing street with this same zip, so write new street

													-- Write new street and address records	
													EXECUTE format('
														INSERT INTO public.mailingstreet(
													            streetid, name, namevariantsarr, citystatezip_cszipid, notes, 
													            pobox)
													    VALUES (DEFAULT, %L, NULL, %L, %L, 
												    	        NULL);',
												    	        street_freshstreetname, citystatezip_rec.id, 'Migration pers-hum AUG-2021; Raw addr: '|| pr.address_street
									    	        );

									    	        -- Now get our fresh street ID for writing our building Number
									    	        SELECT currval('mailingstreet_streetid_seq') INTO street_freshid;
							    	        END IF; -- possibly write new street
							    	        -- with a street PK, we're ready to write to the mailingaddress base table
							    	        EXECUTE format('
							    	        	INSERT INTO public.mailingaddress(
											            addressid, bldgno, street_streetid, verifiedts, verifiedby_userid, 
											            verifiedsource_sourceid, source_sourceid, createdts, createdby_userid, 
											            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
											            notes)
											    VALUES (DEFAULT, %L, %L, NULL, NULL, 
											            NULL, %L, now(), %L, 
											            now(), %L, NULL, NULL, 
											            %L);',
											            extractbuildingno(pr.address_street), street_freshid,
											            defaultsource, creationrobotuser,
											            creationrobotuser,
											            'Migration AUG-2021: Raw addr: ' || pr.address_street
							    	        	);

							    	        SELECT currval('mailingaddress_addressid_seq') INTO address_freshid;

							    	        -- Now that we know the ID of the fresh mailing, we can link our fresh human via the old personid
							    	        -- to this new address we just made and got an ID for
							    	        EXECUTE format('INSERT INTO public.humanmailingaddress(
														            humanmailing_humanid, humanmailing_addressid, source_sourceid, 
														            createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, 
														            deactivatedts, deactivatedby_userid, notes, linkid, linkedobjectrole_lorid)
														    VALUES (%L, %L, %L, 
														            now(), %L, now(), %L, 
														            NULL, NULL, ''Created during person-human migration AUG 2021'', DEFAULT, %L);',
														            current_human_id, address_freshid, defaultsource,
														            creationrobotuser, creationrobotuser, human_mailing_lorid);
						    	        ELSE -- could not extract street
						    	        	RAISE NOTICE 'COULD NOT EXTRACT STREET; SKIPPING NEW ADDRESS INSERT ';
											EXECUTE format ('INSERT INTO public.personhumanmigrationlog(
															            logentryid, human_humanid, person_personid, error_code, notes, ts)
															    VALUES (DEFAULT, %L, %L, %L, %L, now());',
															    current_human_id, pr.personid, 6,
															    'Street parsing failure on: ' ||  pr.address_street
															);
										END IF;


									ELSE -- malformed ZIP on legacy person, meaning we cannot write a new address
									RAISE NOTICE 'ZIP NOT FOUND: % ', zip_parsed;
										EXECUTE format ('INSERT INTO public.personhumanmigrationlog(
														            logentryid, human_humanid, person_personid, error_code, notes, ts)
														    VALUES (DEFAULT, %L, %L, %L, %L, now());',
														    current_human_id, pr.personid, 3, 
														    'Zip not found in master file: ' || pr.address_zip
														);
								END IF; -- end check for legitimate zipcode found on old person address
						END IF; -- end check for existing address connection between person's parcel and ONE of that parcel's addresses
		 		END LOOP; -- end iteration over property-person records 

				RAISE NOTICE 'END PERSON RECORD';
		END LOOP; -- over legacy person table records

		RETURN human_rec_count;
	END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.migratepropertytoparcel(creationrobotuser integer, defaultsource integer, cityid integer, municodetarget integer, parceladdr_lorid integer)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
	DECLARE
	 	pr RECORD;
	 	deacts TIMESTAMP WITH TIME ZONE;
	 	deacuser INTEGER;
	 	extractedbldg TEXT;
	 	extractedstreet TEXT;
	 	addr_range TEXT;
	 	addr_range_start TEXT;
	 	addr_range_end TEXT;
	 	addr_range_start_no INTEGER;
	 	addr_range_end_no INTEGER;
	 	addr_range_cursor INTEGER;
	 	addr_range_arr TEXT[];
	 	bldgno TEXT;
	 	maid INTEGER; -- mailing address ID
	 	current_street_id INTEGER;
	 	buildingcount INTEGER;

	BEGIN
	buildingcount := 0;
		RAISE NOTICE 'starting property migration...';
		FOR pr IN SELECT 		propertyid, municipality_municode, parid, lotandblock, address, 
						       usegroup, constructiontype, countycode, notes, addr_city, addr_state, 
						       addr_zip, ownercode, propclass, lastupdated, lastupdatedby, locationdescription, 
						       bobsource_sourceid, unfitdatestart, unfitdatestop, unfitby_userid, 
						       abandoneddatestart, abandoneddatestop, abandonedby_userid, vacantdatestart, 
						       vacantdatestop, vacantby_userid, condition_intensityclassid, 
						       landbankprospect_intensityclassid, landbankheld, active, nonaddressable, 
						       usetype_typeid, creationts
						  FROM public.property WHERE municipality_municode=municodetarget
		 
		LOOP -- over properties in legacy table
			RAISE NOTICE 'LOOP ITERATION: %; propertyid %; parcel id  %', pr.address, pr.propertyid, pr.parid; 
			-- clear our iterables
			addr_range_arr := ARRAY[]::text[];
			addr_range := NULL;
			-- check for deactivation
			IF NOT pr.active 
			THEN 
				deacts := now();
				deacuser := creationrobotuser; -- the cogbot
			ELSE
				deacts := NULL;
				deacuser := NULL;
			END IF;

			-- TRY manual string concatenation method

			EXECUTE 'INSERT INTO public.parcel(
						            parcelkey, parcelidcnty, source_sourceid, createdts, createdby_userid, 
						            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
						            notes, muni_municode, lotandblock)  VALUES ('
					|| pr.propertyid
					|| ',' 
					|| quote_nullable(unifyspacesandtrim(pr.parid))
					|| ',' 
					|| quote_nullable(pr.bobsource_sourceid)
					|| ',' 
					|| quote_nullable(pr.creationts)
					|| ',' 
					|| creationrobotuser
					|| ',' 
	                || quote_nullable(pr.lastupdated)
					|| ',' 
	                || quote_nullable(pr.lastupdatedby)
					|| ',' 
	                || quote_nullable(deacts)
					|| ',' 
	                || quote_nullable(deacuser)
					|| ',' 
	              	|| quote_nullable(unifyspacesandtrim(pr.notes))
					|| ',' 
	              	|| pr.municipality_municode
					|| ',' 
	              	|| quote_nullable(pr.lotandblock) 	
					|| ');';

			EXECUTE format('INSERT INTO public.parcelinfo(
					            parcelinfoid, parcel_parcelkey, usegroup, constructiontype, countycode, 
					            notes, ownercode, propclass, locationdescription, bobsource_sourceid, 
					            unfitdatestart, unfitdatestop, unfitby_userid, abandoneddatestart, 
					            abandoneddatestop, abandonedby_userid, vacantdatestart, vacantdatestop, 
					            vacantby_userid, condition_intensityclassid, landbankprospect_intensityclassid, 
					            landbankheld, nonaddressable, usetype_typeid, createdts, createdby_userid, 
					            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid)
					    VALUES (DEFAULT, %L, %L, %L, %L, 
					            NULL, %L, %L, NULL, %L, 
					            %L, %L, %L, %L, 
					            %L, %L, %L, %L, 
					            %L, %L, %L, 
					            %L, %L, %L, %L, %L, 
					            %L, %L, %L, %L);',
				            pr.propertyid, pr.usegroup, pr.constructiontype, pr.countycode,
				            pr.ownercode, pr.propclass, pr.bobsource_sourceid,
				            pr.unfitdatestart, pr.unfitdatestop, pr.unfitby_userid, pr.abandoneddatestart, 
			            	pr.abandoneddatestop, pr.abandonedby_userid, pr.vacantdatestart, pr.vacantdatestop, 
			            	pr.vacantby_userid, pr.condition_intensityclassid, pr.landbankprospect_intensityclassid,
			            	pr.landbankheld, pr.nonaddressable, pr.usetype_typeid, pr.creationts, creationrobotuser,
			            	pr.creationts, creationrobotuser, deacts, deacuser
						);

			-- parse address into street and bldgno 
			extractedstreet := extractstreet(pr.address);
			-- See if street is in the table already, if so, get its ID
			
			SELECT streetid INTO current_street_id
				FROM public.mailingstreet 
				WHERE name ILIKE '%'||extractedstreet||'%'
					AND citystatezip_cszipid = cityid; -- only look for existing street matches within the zip of this muni
			
			RAISE NOTICE 'PropID: %; Has street % been found? Street ID: %', pr.address, extractedstreet, current_street_id;
			IF extractedstreet IS NOT NULL
				THEN
					IF FOUND
					THEN -- we have an existing street

						NULL; -- we'll use the current_street_id for all the address writes

					ELSE -- we don't have a record of this street, so write it and grab its ID
						-- write street into mailingstreet
						EXECUTE format('INSERT INTO public.mailingstreet(
										            	streetid, name, namevariantsarr, citystatezip_cszipid, notes, 
		            									pobox)
										    VALUES (DEFAULT, %L, NULL, %L, %L, 
										            NULL);',
										            		  unifyspacesandtrim(extractedstreet), cityid, 'MIGRATION AUG-2021; Raw addr: ' || pr.address );
						-- fetch fresh street id
						SELECT currval('mailingstreet_streetid_seq') INTO current_street_id;
					END IF;
					
					-- extract addresses with a - in there somewhere
					addr_range := substring(pr.address from '\d+\W?-\d+');
					RAISE NOTICE 'FOUND RANGE ADDRESS: %', addr_range;

					IF 
						addr_range IS NOT NULL
					THEN -- build range
						addr_range_start := substring(addr_range from '\d+');
						addr_range_end := substring(addr_range from '\d+\W?-(\d+)');
						addr_range_start_no := CAST (addr_range_start AS INTEGER);
						addr_range_end_no := CAST (addr_range_end AS INTEGER);
						addr_range_cursor := addr_range_start_no;
						WHILE  
							addr_range_cursor <= addr_range_end_no
						LOOP
							addr_range_arr := array_append(addr_range_arr, addr_range_cursor::text);
							RAISE NOTICE 'ADDR RANGE CURSOR VAL: %; ARRAY STATUS: % ', addr_range_cursor, addr_range_arr;
							-- step up by 2 building nos per even/odd numbering schema
							addr_range_cursor := addr_range_cursor + 2; 

						END LOOP;

					ELSE -- NORMAL building no
						extractedbldg := extractbuildingno(pr.address);
						RAISE NOTICE 'FOUND NORMAL BUILDING pr.address: %, extracted no: %', pr.address, extractedbldg;
						addr_range_arr := array_append(addr_range_arr, extractedbldg);
						
					END IF;

					RAISE NOTICE 'INSERTING MAILING ADDRESSES ARRAY: % ', addr_range_arr;

					FOREACH bldgno IN ARRAY addr_range_arr
					LOOP -- over each address in the array
						RAISE NOTICE 'INSERTING BLDG NO: %', bldgno;
						EXECUTE format('INSERT INTO public.mailingaddress(
									            addressid, bldgno, street_streetid, verifiedts, verifiedby_userid, 
									            verifiedsource_sourceid, source_sourceid, createdts, createdby_userid, 
									            lastupdatedts, lastupdatedby_userid, notes)
									    VALUES (DEFAULT, %L, %L, NULL, NULL, 
									            NULL, %L, %L, %L, 
									            %L, %L, %L);',
									                      unifyspacesandtrim(bldgno), current_street_id,
							                     defaultsource, now(), creationrobotuser,
							                    now(), creationrobotuser, 'MIGRATION AUG-2021; Raw Addr: ' || pr.address);
						buildingcount := buildingcount + 1;

						-- get our fresh mailing address ID
						SELECT currval('mailingaddress_addressid_seq') INTO maid;

						-- Connect our current parcel with each
						EXECUTE format('INSERT INTO public.parcelmailingaddress(
										            mailingparcel_parcelid, mailingparcel_mailingid, source_sourceid, 
										            createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, 
										            deactivatedts, deactivatedby_userid, notes, linkid, linkedobjectrole_lorid)
										    VALUES (%L, %L, %L, 
										            now(), %L, now(), %L, 
										            NULL, NULL, ''Created during parcel migration JUL-21'', DEFAULT, %L);',
									            	pr.propertyid, maid, defaultsource,
									            	creationrobotuser, creationrobotuser, 
		            								parceladdr_lorid);
						RAISE NOTICE 'LinkID complete: % ', maid;
					END LOOP; -- over each building Number extracted from the original property address 
				ELSE -- we don't have a well formed street
					EXECUTE format('INSERT INTO public.parcelmigrationlog(
								            logentryid, property_id, parcel_id, error_code, notes, ts)
								    VALUES (DEFAULT, %L, NULL, 1, ''IMPROPERLY FORMED ADDRESS'', now());',
								    pr.propertyid);
				END IF; -- check of malformed property
		END LOOP; -- over properties in the legacy table
		RETURN buildingcount;
	END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.overlaps_2d(box2df, box2df)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_contains_box2df_box2df_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.overlaps_2d(geometry, box2df)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT $2 OPERATOR(public.&&) $1;$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.overlaps_2d(box2df, geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_overlaps_box2df_geom_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.overlaps_geog(geography, gidx)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT $2 OPERATOR(public.&&) $1;$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.overlaps_geog(gidx, gidx)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_gidx_gidx_overlaps$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.overlaps_geog(gidx, geography)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_gidx_geog_overlaps$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.overlaps_nd(gidx, gidx)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_gidx_gidx_overlaps$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.overlaps_nd(geometry, gidx)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT $2 OPERATOR(public.&&&) $1;$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.overlaps_nd(gidx, geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$gserialized_gidx_geom_overlaps$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.path(geometry)
 RETURNS path
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geometry_to_path$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_asgeobuf_finalfn(internal)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$pgis_asgeobuf_finalfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_asgeobuf_transfn(internal, anyelement, text)
 RETURNS internal
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$pgis_asgeobuf_transfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_asgeobuf_transfn(internal, anyelement)
 RETURNS internal
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$pgis_asgeobuf_transfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_asmvt_combinefn(internal, internal)
 RETURNS internal
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$pgis_asmvt_combinefn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_asmvt_deserialfn(bytea, internal)
 RETURNS internal
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$pgis_asmvt_deserialfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_asmvt_finalfn(internal)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$pgis_asmvt_finalfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_asmvt_serialfn(internal)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$pgis_asmvt_serialfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_asmvt_transfn(internal, anyelement)
 RETURNS internal
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$pgis_asmvt_transfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_asmvt_transfn(internal, anyelement, text, integer, text)
 RETURNS internal
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$pgis_asmvt_transfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_asmvt_transfn(internal, anyelement, text)
 RETURNS internal
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$pgis_asmvt_transfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_asmvt_transfn(internal, anyelement, text, integer)
 RETURNS internal
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$pgis_asmvt_transfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_geometry_accum_finalfn(internal)
 RETURNS geometry[]
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$pgis_geometry_accum_finalfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_geometry_accum_transfn(internal, geometry, double precision, integer)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$pgis_geometry_accum_transfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_geometry_accum_transfn(internal, geometry, double precision)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$pgis_geometry_accum_transfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_geometry_accum_transfn(internal, geometry)
 RETURNS internal
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$pgis_geometry_accum_transfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_geometry_clusterintersecting_finalfn(internal)
 RETURNS geometry[]
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$pgis_geometry_clusterintersecting_finalfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_geometry_clusterwithin_finalfn(internal)
 RETURNS geometry[]
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$pgis_geometry_clusterwithin_finalfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_geometry_collect_finalfn(internal)
 RETURNS geometry
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$pgis_geometry_collect_finalfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_geometry_makeline_finalfn(internal)
 RETURNS geometry
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$pgis_geometry_makeline_finalfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_geometry_polygonize_finalfn(internal)
 RETURNS geometry
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$pgis_geometry_polygonize_finalfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgis_geometry_union_finalfn(internal)
 RETURNS geometry
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$pgis_geometry_union_finalfn$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_armor_headers(text, OUT key text, OUT value text)
 RETURNS SETOF record
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pgp_armor_headers$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_key_id(bytea)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pgp_key_id_w$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_pub_decrypt(bytea, bytea)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_decrypt_text$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_pub_decrypt(bytea, bytea, text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_decrypt_text$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_pub_decrypt(bytea, bytea, text, text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_decrypt_text$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_decrypt_bytea$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_decrypt_bytea$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea, text, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_decrypt_bytea$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_pub_encrypt(text, bytea, text)
 RETURNS bytea
 LANGUAGE c
 STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_encrypt_text$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_pub_encrypt(text, bytea)
 RETURNS bytea
 LANGUAGE c
 STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_encrypt_text$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_pub_encrypt_bytea(bytea, bytea, text)
 RETURNS bytea
 LANGUAGE c
 STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_encrypt_bytea$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_pub_encrypt_bytea(bytea, bytea)
 RETURNS bytea
 LANGUAGE c
 STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_encrypt_bytea$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_sym_decrypt(bytea, text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pgp_sym_decrypt_text$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_sym_decrypt(bytea, text, text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pgp_sym_decrypt_text$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_sym_decrypt_bytea(bytea, text, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pgp_sym_decrypt_bytea$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_sym_decrypt_bytea(bytea, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/pgcrypto', $function$pgp_sym_decrypt_bytea$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_sym_encrypt(text, text, text)
 RETURNS bytea
 LANGUAGE c
 STRICT
AS '$libdir/pgcrypto', $function$pgp_sym_encrypt_text$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_sym_encrypt(text, text)
 RETURNS bytea
 LANGUAGE c
 STRICT
AS '$libdir/pgcrypto', $function$pgp_sym_encrypt_text$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_sym_encrypt_bytea(bytea, text)
 RETURNS bytea
 LANGUAGE c
 STRICT
AS '$libdir/pgcrypto', $function$pgp_sym_encrypt_bytea$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.pgp_sym_encrypt_bytea(bytea, text, text)
 RETURNS bytea
 LANGUAGE c
 STRICT
AS '$libdir/pgcrypto', $function$pgp_sym_encrypt_bytea$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.point(geometry)
 RETURNS point
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geometry_to_point$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.polygon(geometry)
 RETURNS polygon
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geometry_to_polygon$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.populate_geometry_columns(use_typmod boolean DEFAULT true)
 RETURNS text
 LANGUAGE plpgsql
AS $function$
DECLARE
	inserted    integer;
	oldcount    integer;
	probed      integer;
	stale       integer;
	gcs         RECORD;
	gc          RECORD;
	gsrid       integer;
	gndims      integer;
	gtype       text;
	query       text;
	gc_is_valid boolean;

BEGIN
	SELECT count(*) INTO oldcount FROM public.geometry_columns;
	inserted := 0;

	-- Count the number of geometry columns in all tables and views
	SELECT count(DISTINCT c.oid) INTO probed
	FROM pg_class c,
		 pg_attribute a,
		 pg_type t,
		 pg_namespace n
	WHERE c.relkind IN('r','v','f')
		AND t.typname = 'geometry'
		AND a.attisdropped = false
		AND a.atttypid = t.oid
		AND a.attrelid = c.oid
		AND c.relnamespace = n.oid
		AND n.nspname NOT ILIKE 'pg_temp%' AND c.relname != 'raster_columns' ;

	-- Iterate through all non-dropped geometry columns
	RAISE DEBUG 'Processing Tables.....';

	FOR gcs IN
	SELECT DISTINCT ON (c.oid) c.oid, n.nspname, c.relname
		FROM pg_class c,
			 pg_attribute a,
			 pg_type t,
			 pg_namespace n
		WHERE c.relkind IN( 'r', 'f')
		AND t.typname = 'geometry'
		AND a.attisdropped = false
		AND a.atttypid = t.oid
		AND a.attrelid = c.oid
		AND c.relnamespace = n.oid
		AND n.nspname NOT ILIKE 'pg_temp%' AND c.relname != 'raster_columns'
	LOOP

		inserted := inserted + public.populate_geometry_columns(gcs.oid, use_typmod);
	END LOOP;

	IF oldcount > inserted THEN
	    stale = oldcount-inserted;
	ELSE
	    stale = 0;
	END IF;

	RETURN 'probed:' ||probed|| ' inserted:'||inserted;
END

$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.populate_geometry_columns(tbl_oid oid, use_typmod boolean DEFAULT true)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
DECLARE
	gcs         RECORD;
	gc          RECORD;
	gc_old      RECORD;
	gsrid       integer;
	gndims      integer;
	gtype       text;
	query       text;
	gc_is_valid boolean;
	inserted    integer;
	constraint_successful boolean := false;

BEGIN
	inserted := 0;

	-- Iterate through all geometry columns in this table
	FOR gcs IN
	SELECT n.nspname, c.relname, a.attname
		FROM pg_class c,
			 pg_attribute a,
			 pg_type t,
			 pg_namespace n
		WHERE c.relkind IN('r', 'f')
		AND t.typname = 'geometry'
		AND a.attisdropped = false
		AND a.atttypid = t.oid
		AND a.attrelid = c.oid
		AND c.relnamespace = n.oid
		AND n.nspname NOT ILIKE 'pg_temp%'
		AND c.oid = tbl_oid
	LOOP

        RAISE DEBUG 'Processing column %.%.%', gcs.nspname, gcs.relname, gcs.attname;

        gc_is_valid := true;
        -- Find the srid, coord_dimension, and type of current geometry
        -- in geometry_columns -- which is now a view

        SELECT type, srid, coord_dimension INTO gc_old
            FROM geometry_columns
            WHERE f_table_schema = gcs.nspname AND f_table_name = gcs.relname AND f_geometry_column = gcs.attname;

        IF upper(gc_old.type) = 'GEOMETRY' THEN
        -- This is an unconstrained geometry we need to do something
        -- We need to figure out what to set the type by inspecting the data
            EXECUTE 'SELECT public.ST_srid(' || quote_ident(gcs.attname) || ') As srid, public.GeometryType(' || quote_ident(gcs.attname) || ') As type, public.ST_NDims(' || quote_ident(gcs.attname) || ') As dims ' ||
                     ' FROM ONLY ' || quote_ident(gcs.nspname) || '.' || quote_ident(gcs.relname) ||
                     ' WHERE ' || quote_ident(gcs.attname) || ' IS NOT NULL LIMIT 1;'
                INTO gc;
            IF gc IS NULL THEN -- there is no data so we can not determine geometry type
            	RAISE WARNING 'No data in table %.%, so no information to determine geometry type and srid', gcs.nspname, gcs.relname;
            	RETURN 0;
            END IF;
            gsrid := gc.srid; gtype := gc.type; gndims := gc.dims;

            IF use_typmod THEN
                BEGIN
                    EXECUTE 'ALTER TABLE ' || quote_ident(gcs.nspname) || '.' || quote_ident(gcs.relname) || ' ALTER COLUMN ' || quote_ident(gcs.attname) ||
                        ' TYPE geometry(' || postgis_type_name(gtype, gndims, true) || ', ' || gsrid::text  || ') ';
                    inserted := inserted + 1;
                EXCEPTION
                        WHEN invalid_parameter_value OR feature_not_supported THEN
                        RAISE WARNING 'Could not convert ''%'' in ''%.%'' to use typmod with srid %, type %: %', quote_ident(gcs.attname), quote_ident(gcs.nspname), quote_ident(gcs.relname), gsrid, postgis_type_name(gtype, gndims, true), SQLERRM;
                            gc_is_valid := false;
                END;

            ELSE
                -- Try to apply srid check to column
            	constraint_successful = false;
                IF (gsrid > 0 AND postgis_constraint_srid(gcs.nspname, gcs.relname,gcs.attname) IS NULL ) THEN
                    BEGIN
                        EXECUTE 'ALTER TABLE ONLY ' || quote_ident(gcs.nspname) || '.' || quote_ident(gcs.relname) ||
                                 ' ADD CONSTRAINT ' || quote_ident('enforce_srid_' || gcs.attname) ||
                                 ' CHECK (ST_srid(' || quote_ident(gcs.attname) || ') = ' || gsrid || ')';
                        constraint_successful := true;
                    EXCEPTION
                        WHEN check_violation THEN
                            RAISE WARNING 'Not inserting ''%'' in ''%.%'' into geometry_columns: could not apply constraint CHECK (st_srid(%) = %)', quote_ident(gcs.attname), quote_ident(gcs.nspname), quote_ident(gcs.relname), quote_ident(gcs.attname), gsrid;
                            gc_is_valid := false;
                    END;
                END IF;

                -- Try to apply ndims check to column
                IF (gndims IS NOT NULL AND postgis_constraint_dims(gcs.nspname, gcs.relname,gcs.attname) IS NULL ) THEN
                    BEGIN
                        EXECUTE 'ALTER TABLE ONLY ' || quote_ident(gcs.nspname) || '.' || quote_ident(gcs.relname) || '
                                 ADD CONSTRAINT ' || quote_ident('enforce_dims_' || gcs.attname) || '
                                 CHECK (st_ndims(' || quote_ident(gcs.attname) || ') = '||gndims||')';
                        constraint_successful := true;
                    EXCEPTION
                        WHEN check_violation THEN
                            RAISE WARNING 'Not inserting ''%'' in ''%.%'' into geometry_columns: could not apply constraint CHECK (st_ndims(%) = %)', quote_ident(gcs.attname), quote_ident(gcs.nspname), quote_ident(gcs.relname), quote_ident(gcs.attname), gndims;
                            gc_is_valid := false;
                    END;
                END IF;

                -- Try to apply geometrytype check to column
                IF (gtype IS NOT NULL AND postgis_constraint_type(gcs.nspname, gcs.relname,gcs.attname) IS NULL ) THEN
                    BEGIN
                        EXECUTE 'ALTER TABLE ONLY ' || quote_ident(gcs.nspname) || '.' || quote_ident(gcs.relname) || '
                        ADD CONSTRAINT ' || quote_ident('enforce_geotype_' || gcs.attname) || '
                        CHECK (geometrytype(' || quote_ident(gcs.attname) || ') = ' || quote_literal(gtype) || ')';
                        constraint_successful := true;
                    EXCEPTION
                        WHEN check_violation THEN
                            -- No geometry check can be applied. This column contains a number of geometry types.
                            RAISE WARNING 'Could not add geometry type check (%) to table column: %.%.%', gtype, quote_ident(gcs.nspname),quote_ident(gcs.relname),quote_ident(gcs.attname);
                    END;
                END IF;
                 --only count if we were successful in applying at least one constraint
                IF constraint_successful THEN
                	inserted := inserted + 1;
                END IF;
            END IF;
	    END IF;

	END LOOP;

	RETURN inserted;
END

$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_addbbox(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_addBBOX$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_cache_bbox()
 RETURNS trigger
 LANGUAGE c
AS '$libdir/postgis-2.5', $function$cache_bbox$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_constraint_dims(geomschema text, geomtable text, geomcolumn text)
 RETURNS integer
 LANGUAGE sql
 STABLE STRICT
AS $function$
SELECT  replace(split_part(s.consrc, ' = ', 2), ')', '')::integer

		 FROM pg_class c, pg_namespace n, pg_attribute a
		 , (SELECT connamespace, conrelid, conkey, pg_get_constraintdef(oid) As consrc
		    FROM pg_constraint) AS s
		 WHERE n.nspname = $1
		 AND c.relname = $2
		 AND a.attname = $3
		 AND a.attrelid = c.oid
		 AND s.connamespace = n.oid
		 AND s.conrelid = c.oid
		 AND a.attnum = ANY (s.conkey)
		 AND s.consrc LIKE '%ndims(% = %';
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_constraint_srid(geomschema text, geomtable text, geomcolumn text)
 RETURNS integer
 LANGUAGE sql
 STABLE STRICT
AS $function$
SELECT replace(replace(split_part(s.consrc, ' = ', 2), ')', ''), '(', '')::integer
		 FROM pg_class c, pg_namespace n, pg_attribute a
		 , (SELECT connamespace, conrelid, conkey, pg_get_constraintdef(oid) As consrc
		    FROM pg_constraint) AS s
		 WHERE n.nspname = $1
		 AND c.relname = $2
		 AND a.attname = $3
		 AND a.attrelid = c.oid
		 AND s.connamespace = n.oid
		 AND s.conrelid = c.oid
		 AND a.attnum = ANY (s.conkey)
		 AND s.consrc LIKE '%srid(% = %';
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_constraint_type(geomschema text, geomtable text, geomcolumn text)
 RETURNS character varying
 LANGUAGE sql
 STABLE STRICT
AS $function$
SELECT  replace(split_part(s.consrc, '''', 2), ')', '')::varchar

		 FROM pg_class c, pg_namespace n, pg_attribute a
		 , (SELECT connamespace, conrelid, conkey, pg_get_constraintdef(oid) As consrc
		    FROM pg_constraint) AS s
		 WHERE n.nspname = $1
		 AND c.relname = $2
		 AND a.attname = $3
		 AND a.attrelid = c.oid
		 AND s.connamespace = n.oid
		 AND s.conrelid = c.oid
		 AND a.attnum = ANY (s.conkey)
		 AND s.consrc LIKE '%geometrytype(% = %';
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_dropbbox(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_dropBBOX$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_extensions_upgrade()
 RETURNS text
 LANGUAGE plpgsql
AS $function$
DECLARE rec record; sql text;
BEGIN
	-- if at a version different from default version or we are at a dev version,
	-- then do an upgrade to default version

	FOR rec in SELECT  name, default_version, installed_version
		FROM pg_available_extensions
		WHERE installed_version > '' AND name IN('postgis', 'postgis_sfcgal', 'postgis_tiger_geocoder', 'postgis_topology')
		AND ( default_version <> installed_version  OR
			( default_version = installed_version AND default_version ILIKE '%dev%' AND  installed_version ILIKE '%dev%'  )  ) LOOP

		-- we need to upgrade to next so our installed is different from current
		-- and then we can upgrade to default_version
		IF rec.installed_version = rec.default_version THEN
			sql = 'ALTER EXTENSION ' || rec.name || ' UPDATE TO ' || quote_ident(rec.default_version || 'next')   || ';';
			EXECUTE sql;
			RAISE NOTICE '%', sql;
		END IF;

		sql = 'ALTER EXTENSION ' || rec.name || ' UPDATE TO ' || quote_ident(rec.default_version)   || ';';
		EXECUTE sql;
		RAISE NOTICE '%', sql;
	END LOOP;

	RETURN public.postgis_full_version();

END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_full_version()
 RETURNS text
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
DECLARE
	libver text;
	svnver text;
	projver text;
	geosver text;
	sfcgalver text;
	cgalver text;
	gdalver text;
	libxmlver text;
	liblwgeomver text;
	dbproc text;
	relproc text;
	fullver text;
	rast_lib_ver text;
	rast_scr_ver text;
	topo_scr_ver text;
	json_lib_ver text;
	protobuf_lib_ver text;
	sfcgal_lib_ver text;
	sfcgal_scr_ver text;
	pgsql_scr_ver text;
	pgsql_ver text;
	core_is_extension bool;
BEGIN
	SELECT public.postgis_lib_version() INTO libver;
	SELECT public.postgis_proj_version() INTO projver;
	SELECT public.postgis_geos_version() INTO geosver;
	SELECT public.postgis_libjson_version() INTO json_lib_ver;
	SELECT public.postgis_libprotobuf_version() INTO protobuf_lib_ver;
	SELECT public._postgis_scripts_pgsql_version() INTO pgsql_scr_ver;
	SELECT public._postgis_pgsql_version() INTO pgsql_ver;
	BEGIN
		SELECT public.postgis_gdal_version() INTO gdalver;
	EXCEPTION
		WHEN undefined_function THEN
			gdalver := NULL;
			RAISE NOTICE 'Function postgis_gdal_version() not found.  Is raster support enabled and rtpostgis.sql installed?';
	END;
	BEGIN
		SELECT public.postgis_sfcgal_version() INTO sfcgalver;
    BEGIN
      SELECT public.postgis_sfcgal_scripts_installed() INTO sfcgal_scr_ver;
    EXCEPTION
      WHEN undefined_function THEN
        sfcgal_scr_ver := 'missing';
    END;
	EXCEPTION
		WHEN undefined_function THEN
			sfcgalver := NULL;
	END;
	SELECT public.postgis_liblwgeom_version() INTO liblwgeomver;
	SELECT public.postgis_libxml_version() INTO libxmlver;
	SELECT public.postgis_scripts_installed() INTO dbproc;
	SELECT public.postgis_scripts_released() INTO relproc;
	select public.postgis_svn_version() INTO svnver;
	BEGIN
		SELECT topology.postgis_topology_scripts_installed() INTO topo_scr_ver;
	EXCEPTION
		WHEN undefined_function OR invalid_schema_name THEN
			topo_scr_ver := NULL;
			RAISE DEBUG 'Function postgis_topology_scripts_installed() not found. Is topology support enabled and topology.sql installed?';
		WHEN insufficient_privilege THEN
			RAISE NOTICE 'Topology support cannot be inspected. Is current user granted USAGE on schema "topology" ?';
		WHEN OTHERS THEN
			RAISE NOTICE 'Function postgis_topology_scripts_installed() could not be called: % (%)', SQLERRM, SQLSTATE;
	END;

	BEGIN
		SELECT postgis_raster_scripts_installed() INTO rast_scr_ver;
	EXCEPTION
		WHEN undefined_function THEN
			rast_scr_ver := NULL;
			RAISE NOTICE 'Function postgis_raster_scripts_installed() not found. Is raster support enabled and rtpostgis.sql installed?';
	END;

	BEGIN
		SELECT public.postgis_raster_lib_version() INTO rast_lib_ver;
	EXCEPTION
		WHEN undefined_function THEN
			rast_lib_ver := NULL;
			RAISE NOTICE 'Function postgis_raster_lib_version() not found. Is raster support enabled and rtpostgis.sql installed?';
	END;

	fullver = 'POSTGIS="' || libver;

	IF  svnver IS NOT NULL THEN
		fullver = fullver || ' r' || svnver;
	END IF;

	fullver = fullver || '"';

	IF EXISTS (
		SELECT * FROM pg_catalog.pg_extension
		WHERE extname = 'postgis')
	THEN
			fullver = fullver || ' [EXTENSION]';
			core_is_extension := true;
	ELSE
			core_is_extension := false;
	END IF;

	IF liblwgeomver != relproc THEN
		fullver = fullver || ' (liblwgeom version mismatch: "' || liblwgeomver || '")';
	END IF;

	fullver = fullver || ' PGSQL="' || pgsql_scr_ver || '"';
	IF pgsql_scr_ver != pgsql_ver THEN
		fullver = fullver || ' (procs need upgrade for use with "' || pgsql_ver || '")';
	END IF;

	IF  geosver IS NOT NULL THEN
		fullver = fullver || ' GEOS="' || geosver || '"';
	END IF;

	IF  sfcgalver IS NOT NULL THEN
		fullver = fullver || ' SFCGAL="' || sfcgalver || '"';
	END IF;

	IF  projver IS NOT NULL THEN
		fullver = fullver || ' PROJ="' || projver || '"';
	END IF;

	IF  gdalver IS NOT NULL THEN
		fullver = fullver || ' GDAL="' || gdalver || '"';
	END IF;

	IF  libxmlver IS NOT NULL THEN
		fullver = fullver || ' LIBXML="' || libxmlver || '"';
	END IF;

	IF json_lib_ver IS NOT NULL THEN
		fullver = fullver || ' LIBJSON="' || json_lib_ver || '"';
	END IF;

	IF protobuf_lib_ver IS NOT NULL THEN
		fullver = fullver || ' LIBPROTOBUF="' || protobuf_lib_ver || '"';
	END IF;

	IF dbproc != relproc THEN
		fullver = fullver || ' (core procs from "' || dbproc || '" need upgrade)';
	END IF;

	IF topo_scr_ver IS NOT NULL THEN
		fullver = fullver || ' TOPOLOGY';
		IF topo_scr_ver != relproc THEN
			fullver = fullver || ' (topology procs from "' || topo_scr_ver || '" need upgrade)';
		END IF;
		IF core_is_extension AND NOT EXISTS (
			SELECT * FROM pg_catalog.pg_extension
			WHERE extname = 'postgis_topology')
		THEN
				fullver = fullver || ' [UNPACKAGED!]';
		END IF;
	END IF;

	IF rast_lib_ver IS NOT NULL THEN
		fullver = fullver || ' RASTER';
		IF rast_lib_ver != relproc THEN
			fullver = fullver || ' (raster lib from "' || rast_lib_ver || '" need upgrade)';
		END IF;
	END IF;

	IF rast_scr_ver IS NOT NULL AND rast_scr_ver != relproc THEN
		fullver = fullver || ' (raster procs from "' || rast_scr_ver || '" need upgrade)';
	END IF;

	IF sfcgal_scr_ver IS NOT NULL AND sfcgal_scr_ver != relproc THEN
    fullver = fullver || ' (sfcgal procs from "' || sfcgal_scr_ver || '" need upgrade)';
	END IF;

	RETURN fullver;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_gdal_version()
 RETURNS text
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_gdal_version$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_geos_version()
 RETURNS text
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$postgis_geos_version$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_getbbox(geometry)
 RETURNS box2d
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_to_BOX2DF$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_hasbbox(geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_hasBBOX$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_lib_build_date()
 RETURNS text
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$postgis_lib_build_date$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_lib_version()
 RETURNS text
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$postgis_lib_version$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_libjson_version()
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$postgis_libjson_version$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_liblwgeom_version()
 RETURNS text
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$postgis_liblwgeom_version$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_libprotobuf_version()
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$postgis_libprotobuf_version$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_libxml_version()
 RETURNS text
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$postgis_libxml_version$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_noop(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$LWGEOM_noop$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_noop(raster)
 RETURNS geometry
 LANGUAGE c
 STABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_noop$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_proj_version()
 RETURNS text
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$postgis_proj_version$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_raster_lib_build_date()
 RETURNS text
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_lib_build_date$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_raster_lib_version()
 RETURNS text
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_lib_version$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_raster_scripts_installed()
 RETURNS text
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT '2.5.5'::text || ' r' || 0::text AS version $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_scripts_build_date()
 RETURNS text
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT '2020-08-17 12:52:20'::text AS version$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_scripts_installed()
 RETURNS text
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT '2.5.5'::text || ' r' || 0::text AS version $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_scripts_released()
 RETURNS text
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$postgis_scripts_released$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_svn_version()
 RETURNS text
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$postgis_svn_version$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_transform_geometry(geometry, text, text, integer)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$transform_geom$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_type_name(geomname character varying, coord_dimension integer, use_new_name boolean DEFAULT true)
 RETURNS character varying
 LANGUAGE sql
 IMMUTABLE STRICT COST 200
AS $function$
	SELECT CASE WHEN $3 THEN new_name ELSE old_name END As geomname
	FROM
	( VALUES
			('GEOMETRY', 'Geometry', 2),
			('GEOMETRY', 'GeometryZ', 3),
			('GEOMETRYM', 'GeometryM', 3),
			('GEOMETRY', 'GeometryZM', 4),

			('GEOMETRYCOLLECTION', 'GeometryCollection', 2),
			('GEOMETRYCOLLECTION', 'GeometryCollectionZ', 3),
			('GEOMETRYCOLLECTIONM', 'GeometryCollectionM', 3),
			('GEOMETRYCOLLECTION', 'GeometryCollectionZM', 4),

			('POINT', 'Point', 2),
			('POINT', 'PointZ', 3),
			('POINTM','PointM', 3),
			('POINT', 'PointZM', 4),

			('MULTIPOINT','MultiPoint', 2),
			('MULTIPOINT','MultiPointZ', 3),
			('MULTIPOINTM','MultiPointM', 3),
			('MULTIPOINT','MultiPointZM', 4),

			('POLYGON', 'Polygon', 2),
			('POLYGON', 'PolygonZ', 3),
			('POLYGONM', 'PolygonM', 3),
			('POLYGON', 'PolygonZM', 4),

			('MULTIPOLYGON', 'MultiPolygon', 2),
			('MULTIPOLYGON', 'MultiPolygonZ', 3),
			('MULTIPOLYGONM', 'MultiPolygonM', 3),
			('MULTIPOLYGON', 'MultiPolygonZM', 4),

			('MULTILINESTRING', 'MultiLineString', 2),
			('MULTILINESTRING', 'MultiLineStringZ', 3),
			('MULTILINESTRINGM', 'MultiLineStringM', 3),
			('MULTILINESTRING', 'MultiLineStringZM', 4),

			('LINESTRING', 'LineString', 2),
			('LINESTRING', 'LineStringZ', 3),
			('LINESTRINGM', 'LineStringM', 3),
			('LINESTRING', 'LineStringZM', 4),

			('CIRCULARSTRING', 'CircularString', 2),
			('CIRCULARSTRING', 'CircularStringZ', 3),
			('CIRCULARSTRINGM', 'CircularStringM' ,3),
			('CIRCULARSTRING', 'CircularStringZM', 4),

			('COMPOUNDCURVE', 'CompoundCurve', 2),
			('COMPOUNDCURVE', 'CompoundCurveZ', 3),
			('COMPOUNDCURVEM', 'CompoundCurveM', 3),
			('COMPOUNDCURVE', 'CompoundCurveZM', 4),

			('CURVEPOLYGON', 'CurvePolygon', 2),
			('CURVEPOLYGON', 'CurvePolygonZ', 3),
			('CURVEPOLYGONM', 'CurvePolygonM', 3),
			('CURVEPOLYGON', 'CurvePolygonZM', 4),

			('MULTICURVE', 'MultiCurve', 2),
			('MULTICURVE', 'MultiCurveZ', 3),
			('MULTICURVEM', 'MultiCurveM', 3),
			('MULTICURVE', 'MultiCurveZM', 4),

			('MULTISURFACE', 'MultiSurface', 2),
			('MULTISURFACE', 'MultiSurfaceZ', 3),
			('MULTISURFACEM', 'MultiSurfaceM', 3),
			('MULTISURFACE', 'MultiSurfaceZM', 4),

			('POLYHEDRALSURFACE', 'PolyhedralSurface', 2),
			('POLYHEDRALSURFACE', 'PolyhedralSurfaceZ', 3),
			('POLYHEDRALSURFACEM', 'PolyhedralSurfaceM', 3),
			('POLYHEDRALSURFACE', 'PolyhedralSurfaceZM', 4),

			('TRIANGLE', 'Triangle', 2),
			('TRIANGLE', 'TriangleZ', 3),
			('TRIANGLEM', 'TriangleM', 3),
			('TRIANGLE', 'TriangleZM', 4),

			('TIN', 'Tin', 2),
			('TIN', 'TinZ', 3),
			('TINM', 'TinM', 3),
			('TIN', 'TinZM', 4) )
			 As g(old_name, new_name, coord_dimension)
	WHERE (upper(old_name) = upper($1) OR upper(new_name) = upper($1))
		AND coord_dimension = $2;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_typmod_dims(integer)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$postgis_typmod_dims$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_typmod_srid(integer)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$postgis_typmod_srid$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_typmod_type(integer)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$postgis_typmod_type$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.postgis_version()
 RETURNS text
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$postgis_version$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.raster_above(raster, raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select $1::public.geometry |>> $2::public.geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.raster_below(raster, raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select $1::public.geometry <<| $2::public.geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.raster_contain(raster, raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select $1::public.geometry ~ $2::public.geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.raster_contained(raster, raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select $1::public.geometry OPERATOR(public.@) $2::public.geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.raster_contained_by_geometry(raster, geometry)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select $1::public.geometry OPERATOR(public.@) $2$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.raster_eq(raster, raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.raster_hash($1) = public.raster_hash($2) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.raster_geometry_contain(raster, geometry)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select $1::public.geometry ~ $2$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.raster_geometry_overlap(raster, geometry)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select $1::public.geometry OPERATOR(public.&&) $2$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.raster_in(cstring)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_in$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.raster_left(raster, raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select $1::public.geometry << $2::public.geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.raster_out(raster)
 RETURNS cstring
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_out$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.raster_overabove(raster, raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select $1::public.geometry |&> $2::public.geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.raster_overbelow(raster, raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select $1::public.geometry &<| $2::public.geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.raster_overlap(raster, raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select $1::public.geometry OPERATOR(public.&&) $2::public.geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.raster_overleft(raster, raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select $1::public.geometry &< $2::public.geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.raster_overright(raster, raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select $1::public.geometry &> $2::public.geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.raster_right(raster, raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select $1::public.geometry >> $2::public.geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.raster_same(raster, raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$select $1::public.geometry ~= $2::public.geometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.resetsequences(incr integer DEFAULT 3)
 RETURNS void
 LANGUAGE plpgsql
AS $function$
declare
    propertyid_ integer;
    unitid_     integer;
    caseid_     integer;
    personid_   integer;
BEGIN
    propertyid_ := MAX(propertyid) FROM property;
        propertyid_ = propertyid_ + incr;
        PERFORM setval('propertyid_seq', propertyid_);
    unitid_ := MAX(unitid) FROM propertyunit;
        unitid_ = unitid_ + incr;
        PERFORM setval('propertunit_unitid_seq', unitid_);
    caseid_ := MAX(caseid) FROM cecase;
        caseid_ = caseid_ + incr;
        PERFORM setval('cecase_caseid_seq', unitid_);
    personid_ := MAX(personid) FROM person;
        personid_ = personid_ + incr;
        PERFORM setval('person_personidseq', personid_);
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.soundex(text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/fuzzystrmatch', $function$soundex$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.spheroid_in(cstring)
 RETURNS spheroid
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ellipsoid_in$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.spheroid_out(spheroid)
 RETURNS cstring
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ellipsoid_out$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_3dclosestpoint(geom1 geometry, geom2 geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_closestpoint3d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_3ddfullywithin(geom1 geometry, geom2 geometry, double precision)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.&&) public.ST_Expand($2,$3) AND $2 OPERATOR(public.&&) public.ST_Expand($1,$3) AND public._ST_3DDFullyWithin($1, $2, $3)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_3ddistance(geom1 geometry, geom2 geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$distance3d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_3ddwithin(geom1 geometry, geom2 geometry, double precision)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.&&) public.ST_Expand($2,$3) AND $2 OPERATOR(public.&&) public.ST_Expand($1,$3) AND public._ST_3DDWithin($1, $2, $3)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_3dintersects(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.&&) $2 AND public._ST_3DIntersects($1, $2)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_3dlength(geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 20
AS '$libdir/postgis-2.5', $function$LWGEOM_length_linestring$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_3dlength_spheroid(geometry, spheroid)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_3DLength_Spheroid', 'ST_LengthSpheroid', '2.2.0');
    SELECT public.ST_LengthSpheroid($1,$2);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_3dlongestline(geom1 geometry, geom2 geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_longestline3d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_3dmakebox(geom1 geometry, geom2 geometry)
 RETURNS box3d
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX3D_construct$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_3dmaxdistance(geom1 geometry, geom2 geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$LWGEOM_maxdistance3d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_3dperimeter(geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$LWGEOM_perimeter_poly$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_3dshortestline(geom1 geometry, geom2 geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_shortestline3d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_addband(torast raster, fromrast raster, fromband integer DEFAULT 1, torastindex integer DEFAULT NULL::integer)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_copyBand$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_addband(rast raster, outdbfile text, outdbindex integer[], index integer DEFAULT NULL::integer, nodataval double precision DEFAULT NULL::double precision)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_AddBand($1, $4, $2, $3, $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_addband(rast raster, index integer, pixeltype text, initialvalue double precision DEFAULT '0'::numeric, nodataval double precision DEFAULT NULL::double precision)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT  public.ST_addband($1, ARRAY[ROW($2, $3, $4, $5)]::addbandarg[]) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_addband(rast raster, index integer, outdbfile text, outdbindex integer[], nodataval double precision DEFAULT NULL::double precision)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_addBandOutDB$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_addband(rast raster, addbandargset addbandarg[])
 RETURNS raster
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_addBand$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_addband(torast raster, fromrasts raster[], fromband integer DEFAULT 1, torastindex integer DEFAULT NULL::integer)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_addBandRasterArray$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_addband(rast raster, pixeltype text, initialvalue double precision DEFAULT '0'::numeric, nodataval double precision DEFAULT NULL::double precision)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT  public.ST_addband($1, ARRAY[ROW(NULL, $2, $3, $4)]::addbandarg[]) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_addmeasure(geometry, double precision, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_AddMeasure$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_addpoint(geom1 geometry, geom2 geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_addpoint$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_addpoint(geom1 geometry, geom2 geometry, integer)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_addpoint$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_affine(geometry, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_affine$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_affine(geometry, double precision, double precision, double precision, double precision, double precision, double precision)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_Affine($1,  $2, $3, 0,  $4, $5, 0,  0, 0, 1,  $6, $7, 0)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_angle(pt1 geometry, pt2 geometry, pt3 geometry, pt4 geometry DEFAULT '0101000000000000000000F87F000000000000F87F'::geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_angle$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_angle(line1 geometry, line2 geometry)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT ST_Angle(St_StartPoint($1), ST_EndPoint($1), St_StartPoint($2), ST_EndPoint($2))$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxcount(rast raster, nband integer, sample_percent double precision)
 RETURNS bigint
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_count($1, $2, TRUE, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxcount(rastertable text, rastercolumn text, nband integer, sample_percent double precision)
 RETURNS bigint
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_count($1, $2, $3, TRUE, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxcount(rastertable text, rastercolumn text, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, sample_percent double precision DEFAULT 0.1)
 RETURNS bigint
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_count($1, $2, $3, $4, $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxcount(rast raster, sample_percent double precision)
 RETURNS bigint
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_count($1, 1, TRUE, $2) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxcount(rastertable text, rastercolumn text, exclude_nodata_value boolean, sample_percent double precision DEFAULT 0.1)
 RETURNS bigint
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_count($1, $2, 1, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxcount(rast raster, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, sample_percent double precision DEFAULT 0.1)
 RETURNS bigint
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_count($1, $2, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxcount(rastertable text, rastercolumn text, sample_percent double precision)
 RETURNS bigint
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_count($1, $2, 1, TRUE, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxcount(rast raster, exclude_nodata_value boolean, sample_percent double precision DEFAULT 0.1)
 RETURNS bigint
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_count($1, 1, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxhistogram(rast raster, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, sample_percent double precision DEFAULT 0.1, bins integer DEFAULT 0, width double precision[] DEFAULT NULL::double precision[], "right" boolean DEFAULT false, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT min, max, count, percent FROM public._ST_histogram($1, $2, $3, $4, $5, $6, $7) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxhistogram(rast raster, nband integer, exclude_nodata_value boolean, sample_percent double precision, bins integer, "right" boolean, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT min, max, count, percent FROM public._ST_histogram($1, $2, $3, $4, $5, NULL, $6) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxhistogram(rast raster, nband integer, sample_percent double precision, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT min, max, count, percent FROM public._ST_histogram($1, $2, TRUE, $3, 0, NULL, FALSE) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxhistogram(rast raster, sample_percent double precision, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT min, max, count, percent FROM public._ST_histogram($1, 1, TRUE, $2, 0, NULL, FALSE) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxhistogram(rastertable text, rastercolumn text, nband integer, sample_percent double precision, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_histogram($1, $2, $3, TRUE, $4, 0, NULL, FALSE) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxhistogram(rast raster, nband integer, sample_percent double precision, bins integer, width double precision[] DEFAULT NULL::double precision[], "right" boolean DEFAULT false, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT min, max, count, percent FROM public._ST_histogram($1, $2, TRUE, $3, $4, $5, $6) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxhistogram(rast raster, nband integer, sample_percent double precision, bins integer, "right" boolean, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT min, max, count, percent FROM public._ST_histogram($1, $2, TRUE, $3, $4, NULL, $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxhistogram(rastertable text, rastercolumn text, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, sample_percent double precision DEFAULT 0.1, bins integer DEFAULT 0, width double precision[] DEFAULT NULL::double precision[], "right" boolean DEFAULT false, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE
AS $function$ SELECT public._ST_histogram($1, $2, $3, $4, $5, $6, $7, $8) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxhistogram(rastertable text, rastercolumn text, sample_percent double precision, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_histogram($1, $2, 1, TRUE, $3, 0, NULL, FALSE) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxhistogram(rastertable text, rastercolumn text, nband integer, sample_percent double precision, bins integer, width double precision[] DEFAULT NULL::double precision[], "right" boolean DEFAULT false, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_histogram($1, $2, $3, TRUE, $4, $5, $6, $7) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxhistogram(rastertable text, rastercolumn text, nband integer, sample_percent double precision, bins integer, "right" boolean, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_histogram($1, $2, $3, TRUE, $4, $5, NULL, $6) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxhistogram(rastertable text, rastercolumn text, nband integer, exclude_nodata_value boolean, sample_percent double precision, bins integer, "right" boolean, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_histogram($1, $2, $3, $4, $5, $6, NULL, $7) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxquantile(rastertable text, rastercolumn text, quantile double precision)
 RETURNS double precision
 LANGUAGE sql
 STABLE
AS $function$ SELECT ( public._ST_quantile($1, $2, 1, TRUE, 0.1, ARRAY[$3]::double precision[])).value $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxquantile(rast raster, sample_percent double precision, quantiles double precision[] DEFAULT NULL::double precision[], OUT quantile double precision, OUT value double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_quantile($1, 1, TRUE, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxquantile(rast raster, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, sample_percent double precision DEFAULT 0.1, quantiles double precision[] DEFAULT NULL::double precision[], OUT quantile double precision, OUT value double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_quantile($1, $2, $3, $4, $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxquantile(rast raster, quantiles double precision[], OUT quantile double precision, OUT value double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_quantile($1, 1, TRUE, 0.1, $2) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxquantile(rast raster, nband integer, exclude_nodata_value boolean, sample_percent double precision, quantile double precision)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT ( public._ST_quantile($1, $2, $3, $4, ARRAY[$5]::double precision[])).value $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxquantile(rast raster, nband integer, sample_percent double precision, quantile double precision)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT ( public._ST_quantile($1, $2, TRUE, $3, ARRAY[$4]::double precision[])).value $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxquantile(rast raster, sample_percent double precision, quantile double precision)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT ( public._ST_quantile($1, 1, TRUE, $2, ARRAY[$3]::double precision[])).value $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxquantile(rast raster, exclude_nodata_value boolean, quantile double precision DEFAULT NULL::double precision)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT ( public._ST_quantile($1, 1, $2, 0.1, ARRAY[$3]::double precision[])).value $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxquantile(rast raster, quantile double precision)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT ( public._ST_quantile($1, 1, TRUE, 0.1, ARRAY[$2]::double precision[])).value $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxquantile(rastertable text, rastercolumn text, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, sample_percent double precision DEFAULT 0.1, quantiles double precision[] DEFAULT NULL::double precision[], OUT quantile double precision, OUT value double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE
AS $function$ SELECT public._ST_quantile($1, $2, $3, $4, $5, $6) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxquantile(rastertable text, rastercolumn text, nband integer, sample_percent double precision, quantiles double precision[] DEFAULT NULL::double precision[], OUT quantile double precision, OUT value double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE
AS $function$ SELECT public._ST_quantile($1, $2, $3, TRUE, $4, $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxquantile(rastertable text, rastercolumn text, sample_percent double precision, quantiles double precision[] DEFAULT NULL::double precision[], OUT quantile double precision, OUT value double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE
AS $function$ SELECT public._ST_quantile($1, $2, 1, TRUE, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxquantile(rastertable text, rastercolumn text, quantiles double precision[], OUT quantile double precision, OUT value double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_quantile($1, $2, 1, TRUE, 0.1, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxquantile(rastertable text, rastercolumn text, nband integer, exclude_nodata_value boolean, sample_percent double precision, quantile double precision)
 RETURNS double precision
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT ( public._ST_quantile($1, $2, $3, $4, $5, ARRAY[$6]::double precision[])).value $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxquantile(rastertable text, rastercolumn text, nband integer, sample_percent double precision, quantile double precision)
 RETURNS double precision
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT ( public._ST_quantile($1, $2, $3, TRUE, $4, ARRAY[$5]::double precision[])).value $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxquantile(rastertable text, rastercolumn text, sample_percent double precision, quantile double precision)
 RETURNS double precision
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT ( public._ST_quantile($1, $2, 1, TRUE, $3, ARRAY[$4]::double precision[])).value $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxquantile(rastertable text, rastercolumn text, exclude_nodata_value boolean, quantile double precision DEFAULT NULL::double precision)
 RETURNS double precision
 LANGUAGE sql
 STABLE
AS $function$ SELECT ( public._ST_quantile($1, $2, 1, $3, 0.1, ARRAY[$4]::double precision[])).value $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxquantile(rast raster, nband integer, sample_percent double precision, quantiles double precision[] DEFAULT NULL::double precision[], OUT quantile double precision, OUT value double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_quantile($1, $2, TRUE, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxsummarystats(rast raster, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, sample_percent double precision DEFAULT 0.1)
 RETURNS summarystats
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_summarystats($1, $2, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxsummarystats(rastertable text, rastercolumn text, nband integer, sample_percent double precision)
 RETURNS summarystats
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_summarystats($1, $2, $3, TRUE, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxsummarystats(rast raster, sample_percent double precision)
 RETURNS summarystats
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_summarystats($1, 1, TRUE, $2) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxsummarystats(rastertable text, rastercolumn text, sample_percent double precision)
 RETURNS summarystats
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_summarystats($1, $2, 1, TRUE, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxsummarystats(rastertable text, rastercolumn text, exclude_nodata_value boolean)
 RETURNS summarystats
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_summarystats($1, $2, 1, $3, 0.1) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxsummarystats(rast raster, exclude_nodata_value boolean, sample_percent double precision DEFAULT 0.1)
 RETURNS summarystats
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_summarystats($1, 1, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxsummarystats(rast raster, nband integer, sample_percent double precision)
 RETURNS summarystats
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_summarystats($1, $2, TRUE, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_approxsummarystats(rastertable text, rastercolumn text, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, sample_percent double precision DEFAULT 0.1)
 RETURNS summarystats
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_summarystats($1, $2, $3, $4, $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_area(text)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_Area($1::public.geometry);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_area(geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$area$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_area(geog geography, use_spheroid boolean DEFAULT true)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$geography_area$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_area2d(geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$LWGEOM_area_polygon$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asbinary(geometry, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$LWGEOM_asBinary$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asbinary(geography, text)
 RETURNS bytea
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_AsBinary($1::public.geometry, $2);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asbinary(raster, outasin boolean DEFAULT false)
 RETURNS bytea
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_AsWKB($1, $2) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asbinary(geography)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_asBinary$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asbinary(geometry)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$LWGEOM_asBinary$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asencodedpolyline(geom geometry, integer DEFAULT 5)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_asEncodedPolyline$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asewkb(geometry)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$WKBFromLWGEOM$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asewkb(geometry, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$WKBFromLWGEOM$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asewkt(text)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_AsEWKT($1::public.geometry);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asewkt(geography)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_asEWKT$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asewkt(geometry)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT COST 750
AS '$libdir/postgis-2.5', $function$LWGEOM_asEWKT$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asgdalraster(rast raster, format text, options text[] DEFAULT NULL::text[], srid integer DEFAULT NULL::integer)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_asGDALRaster$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asgeojson(gj_version integer, geom geometry, maxdecimaldigits integer DEFAULT 15, options integer DEFAULT 0)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_AsGeoJson($2::public.geometry, $3::int4, $4::int4); $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asgeojson(text)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_AsGeoJson(1, $1::public.geometry,15,0);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asgeojson(gj_version integer, geog geography, maxdecimaldigits integer DEFAULT 15, options integer DEFAULT 0)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_AsGeoJson($1, $2, $3, $4); $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asgeojson(geom geometry, maxdecimaldigits integer DEFAULT 15, options integer DEFAULT 0)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT COST 1000
AS '$libdir/postgis-2.5', $function$LWGEOM_asGeoJson$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asgeojson(geog geography, maxdecimaldigits integer DEFAULT 15, options integer DEFAULT 0)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_AsGeoJson(1, $1, $2, $3); $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asgml(geom geometry, maxdecimaldigits integer DEFAULT 15, options integer DEFAULT 0)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_AsGML(2, $1, $2, $3, null, null); $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asgml(text)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_AsGML(2,$1::public.geometry,15,0, NULL, NULL);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asgml(geog geography, maxdecimaldigits integer DEFAULT 15, options integer DEFAULT 0)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public._ST_AsGML(2, $1, $2, $3, null, null)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asgml(version integer, geog geography, maxdecimaldigits integer DEFAULT 15, options integer DEFAULT 0, nprefix text DEFAULT NULL::text, id text DEFAULT NULL::text)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_AsGML($1, $2, $3, $4, $5, $6);$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asgml(version integer, geom geometry, maxdecimaldigits integer DEFAULT 15, options integer DEFAULT 0, nprefix text DEFAULT NULL::text, id text DEFAULT NULL::text)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_AsGML($1, $2, $3, $4, $5, $6); $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_ashexewkb(geometry)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT COST 25
AS '$libdir/postgis-2.5', $function$LWGEOM_asHEXEWKB$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_ashexewkb(geometry, text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT COST 25
AS '$libdir/postgis-2.5', $function$LWGEOM_asHEXEWKB$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_ashexwkb(raster, outasin boolean DEFAULT false)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_asHexWKB$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asjpeg(rast raster, nbands integer[], quality integer)
 RETURNS bytea
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
	DECLARE
		quality2 int;
		options text[];
	BEGIN
		IF quality IS NOT NULL THEN
			IF quality > 100 THEN
				quality2 := 100;
			ELSEIF quality < 10 THEN
				quality2 := 10;
			ELSE
				quality2 := quality;
			END IF;

			options := array_append(options, 'QUALITY=' || quality2);
		END IF;

		RETURN public.st_asjpeg(st_band($1, $2), options);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asjpeg(rast raster, nbands integer[], options text[] DEFAULT NULL::text[])
 RETURNS bytea
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT st_asjpeg(st_band($1, $2), $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asjpeg(rast raster, nband integer, quality integer)
 RETURNS bytea
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.st_asjpeg($1, ARRAY[$2], $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asjpeg(rast raster, nband integer, options text[] DEFAULT NULL::text[])
 RETURNS bytea
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.st_asjpeg(st_band($1, $2), $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asjpeg(rast raster, options text[] DEFAULT NULL::text[])
 RETURNS bytea
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		rast2 public.raster;
		num_bands int;
		i int;
	BEGIN
		IF rast IS NULL THEN
			RETURN NULL;
		END IF;

		num_bands := st_numbands($1);

		-- JPEG allows 1 or 3 bands
		IF num_bands <> 1 AND num_bands <> 3 THEN
			RAISE NOTICE 'The JPEG format only permits one or three bands.  The first band will be used.';
			rast2 := st_band(rast, ARRAY[1]);
			num_bands := st_numbands(rast);
		ELSE
			rast2 := rast;
		END IF;

		-- JPEG only supports 8BUI pixeltype
		FOR i IN 1..num_bands LOOP
			IF public.ST_BandPixelType(rast, i) != '8BUI' THEN
				RAISE EXCEPTION 'The pixel type of band % in the raster is not 8BUI.  The JPEG format can only be used with the 8BUI pixel type.', i;
			END IF;
		END LOOP;

		RETURN st_asgdalraster(rast2, 'JPEG', $2, NULL);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_askml(geog geography, maxdecimaldigits integer DEFAULT 15)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public._ST_AsKML(2, $1, $2, null)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_askml(version integer, geom geometry, maxdecimaldigits integer DEFAULT 15, nprefix text DEFAULT NULL::text)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_AsKML($1, public.ST_Transform($2,4326), $3, $4); $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_askml(version integer, geog geography, maxdecimaldigits integer DEFAULT 15, nprefix text DEFAULT NULL::text)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT public._ST_AsKML($1, $2, $3, $4)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_askml(geom geometry, maxdecimaldigits integer DEFAULT 15)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_AsKML(2, ST_Transform($1,4326), $2, null); $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_askml(text)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_AsKML(2, $1::public.geometry, 15, null);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_aslatlontext(geom geometry, tmpl text DEFAULT ''::text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_to_latlon$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asmvtgeom(geom geometry, bounds box2d, extent integer DEFAULT 4096, buffer integer DEFAULT 256, clip_geom boolean DEFAULT true)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$ST_AsMVTGeom$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_aspect(rast raster, nband integer DEFAULT 1, pixeltype text DEFAULT '32BF'::text, units text DEFAULT 'DEGREES'::text, interpolate_nodata boolean DEFAULT false)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_aspect($1, $2, NULL::public.raster, $3, $4, $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_aspect(rast raster, nband integer, customextent raster, pixeltype text DEFAULT '32BF'::text, units text DEFAULT 'DEGREES'::text, interpolate_nodata boolean DEFAULT false)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		_rast public.raster;
		_nband integer;
		_pixtype text;
		_width integer;
		_height integer;
		_customextent public.raster;
		_extenttype text;
	BEGIN
		_customextent := customextent;
		IF _customextent IS NULL THEN
			_extenttype := 'FIRST';
		ELSE
			_extenttype := 'CUSTOM';
		END IF;

		IF interpolate_nodata IS TRUE THEN
			_rast := public.ST_MapAlgebra(
				ARRAY[ROW(rast, nband)]::rastbandarg[],
				'public.st_invdistweight4ma(double precision[][][], integer[][], text[])'::regprocedure,
				pixeltype,
				'FIRST', NULL,
				1, 1
			);
			_nband := 1;
			_pixtype := NULL;
		ELSE
			_rast := rast;
			_nband := nband;
			_pixtype := pixeltype;
		END IF;

		-- get properties
		SELECT width, height INTO _width, _height FROM public.ST_Metadata(_rast);

		RETURN public.ST_MapAlgebra(
			ARRAY[ROW(_rast, _nband)]::rastbandarg[],
			' public._ST_aspect4ma(double precision[][][], integer[][], text[])'::regprocedure,
			_pixtype,
			_extenttype, _customextent,
			1, 1,
			_width::text, _height::text,
			units::text
		);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_aspng(rast raster, nbands integer[], options text[] DEFAULT NULL::text[])
 RETURNS bytea
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.st_aspng(st_band($1, $2), $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_aspng(rast raster, nbands integer[], compression integer)
 RETURNS bytea
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
	DECLARE
		compression2 int;
		options text[];
	BEGIN
		IF compression IS NOT NULL THEN
			IF compression > 9 THEN
				compression2 := 9;
			ELSEIF compression < 1 THEN
				compression2 := 1;
			ELSE
				compression2 := compression;
			END IF;

			options := array_append(options, 'ZLEVEL=' || compression2);
		END IF;

		RETURN public.st_aspng(st_band($1, $2), options);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_aspng(rast raster, options text[] DEFAULT NULL::text[])
 RETURNS bytea
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		rast2 public.raster;
		num_bands int;
		i int;
		pt text;
	BEGIN
		IF rast IS NULL THEN
			RETURN NULL;
		END IF;

		num_bands := st_numbands($1);

		-- PNG allows 1, 3 or 4 bands
		IF num_bands <> 1 AND num_bands <> 3 AND num_bands <> 4 THEN
			RAISE NOTICE 'The PNG format only permits one, three or four bands.  The first band will be used.';
			rast2 := public.st_band($1, ARRAY[1]);
			num_bands := public.st_numbands(rast2);
		ELSE
			rast2 := rast;
		END IF;

		-- PNG only supports 8BUI and 16BUI pixeltype
		FOR i IN 1..num_bands LOOP
			pt = public.ST_BandPixelType(rast, i);
			IF pt != '8BUI' AND pt != '16BUI' THEN
				RAISE EXCEPTION 'The pixel type of band % in the raster is not 8BUI or 16BUI.  The PNG format can only be used with 8BUI and 16BUI pixel types.', i;
			END IF;
		END LOOP;

		RETURN public.st_asgdalraster(rast2, 'PNG', $2, NULL);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_aspng(rast raster, nband integer, compression integer)
 RETURNS bytea
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.st_aspng($1, ARRAY[$2], $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_aspng(rast raster, nband integer, options text[] DEFAULT NULL::text[])
 RETURNS bytea
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.st_aspng(st_band($1, $2), $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asraster(geom geometry, width integer, height integer, gridx double precision, gridy double precision, pixeltype text, value double precision DEFAULT 1, nodataval double precision DEFAULT 0, skewx double precision DEFAULT 0, skewy double precision DEFAULT 0, touched boolean DEFAULT false)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_asraster($1, NULL, NULL, $2, $3, ARRAY[$6]::text[], ARRAY[$7]::double precision[], ARRAY[$8]::double precision[], NULL, NULL, $4, $5, $9, $10, $11) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asraster(geom geometry, width integer, height integer, pixeltype text[], value double precision[] DEFAULT ARRAY[(1)::double precision], nodataval double precision[] DEFAULT ARRAY[(0)::double precision], upperleftx double precision DEFAULT NULL::double precision, upperlefty double precision DEFAULT NULL::double precision, skewx double precision DEFAULT 0, skewy double precision DEFAULT 0, touched boolean DEFAULT false)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_asraster($1, NULL, NULL, $2, $3, $4, $5, $6, $7, $8, NULL, NULL,	$9, $10, $11) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asraster(geom geometry, width integer, height integer, gridx double precision DEFAULT NULL::double precision, gridy double precision DEFAULT NULL::double precision, pixeltype text[] DEFAULT ARRAY['8BUI'::text], value double precision[] DEFAULT ARRAY[(1)::double precision], nodataval double precision[] DEFAULT ARRAY[(0)::double precision], skewx double precision DEFAULT 0, skewy double precision DEFAULT 0, touched boolean DEFAULT false)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_asraster($1, NULL, NULL, $2, $3, $6, $7, $8, NULL, NULL, $4, $5, $9, $10, $11) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asraster(geom geometry, ref raster, pixeltype text, value double precision DEFAULT 1, nodataval double precision DEFAULT 0, touched boolean DEFAULT false)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT  public.ST_AsRaster($1, $2, ARRAY[$3]::text[], ARRAY[$4]::double precision[], ARRAY[$5]::double precision[], $6) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asraster(geom geometry, ref raster, pixeltype text[] DEFAULT ARRAY['8BUI'::text], value double precision[] DEFAULT ARRAY[(1)::double precision], nodataval double precision[] DEFAULT ARRAY[(0)::double precision], touched boolean DEFAULT false)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		g public.geometry;
		g_srid integer;

		ul_x double precision;
		ul_y double precision;
		scale_x double precision;
		scale_y double precision;
		skew_x double precision;
		skew_y double precision;
		sr_id integer;
	BEGIN
		SELECT upperleftx, upperlefty, scalex, scaley, skewx, skewy, srid INTO ul_x, ul_y, scale_x, scale_y, skew_x, skew_y, sr_id FROM public.ST_Metadata(ref);
		--RAISE NOTICE '%, %, %, %, %, %, %', ul_x, ul_y, scale_x, scale_y, skew_x, skew_y, sr_id;

		-- geometry and raster has different SRID
		g_srid := public.ST_SRID(geom);
		IF g_srid != sr_id THEN
			RAISE NOTICE 'The geometry''s SRID (%) is not the same as the raster''s SRID (%).  The geometry will be transformed to the raster''s projection', g_srid, sr_id;
			g := public.ST_Transform(geom, sr_id);
		ELSE
			g := geom;
		END IF;

		RETURN public._ST_asraster(g, scale_x, scale_y, NULL, NULL, $3, $4, $5, NULL, NULL, ul_x, ul_y, skew_x, skew_y, $6);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asraster(geom geometry, scalex double precision, scaley double precision, gridx double precision, gridy double precision, pixeltype text, value double precision DEFAULT 1, nodataval double precision DEFAULT 0, skewx double precision DEFAULT 0, skewy double precision DEFAULT 0, touched boolean DEFAULT false)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_asraster($1, $2, $3, NULL, NULL, ARRAY[$6]::text[], ARRAY[$7]::double precision[], ARRAY[$8]::double precision[], NULL, NULL, $4, $5, $9, $10, $11) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asraster(geom geometry, scalex double precision, scaley double precision, pixeltype text, value double precision DEFAULT 1, nodataval double precision DEFAULT 0, upperleftx double precision DEFAULT NULL::double precision, upperlefty double precision DEFAULT NULL::double precision, skewx double precision DEFAULT 0, skewy double precision DEFAULT 0, touched boolean DEFAULT false)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_asraster($1, $2, $3, NULL, NULL, ARRAY[$4]::text[], ARRAY[$5]::double precision[], ARRAY[$6]::double precision[], $7, $8, NULL, NULL, $9, $10, $11) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asraster(geom geometry, width integer, height integer, pixeltype text, value double precision DEFAULT 1, nodataval double precision DEFAULT 0, upperleftx double precision DEFAULT NULL::double precision, upperlefty double precision DEFAULT NULL::double precision, skewx double precision DEFAULT 0, skewy double precision DEFAULT 0, touched boolean DEFAULT false)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_asraster($1, NULL, NULL, $2, $3, ARRAY[$4]::text[], ARRAY[$5]::double precision[], ARRAY[$6]::double precision[], $7, $8, NULL, NULL,$9, $10, $11) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asraster(geom geometry, scalex double precision, scaley double precision, pixeltype text[], value double precision[] DEFAULT ARRAY[(1)::double precision], nodataval double precision[] DEFAULT ARRAY[(0)::double precision], upperleftx double precision DEFAULT NULL::double precision, upperlefty double precision DEFAULT NULL::double precision, skewx double precision DEFAULT 0, skewy double precision DEFAULT 0, touched boolean DEFAULT false)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_asraster($1, $2, $3, NULL, NULL, $4, $5, $6, $7, $8, NULL, NULL,	$9, $10, $11) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asraster(geom geometry, scalex double precision, scaley double precision, gridx double precision DEFAULT NULL::double precision, gridy double precision DEFAULT NULL::double precision, pixeltype text[] DEFAULT ARRAY['8BUI'::text], value double precision[] DEFAULT ARRAY[(1)::double precision], nodataval double precision[] DEFAULT ARRAY[(0)::double precision], skewx double precision DEFAULT 0, skewy double precision DEFAULT 0, touched boolean DEFAULT false)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_asraster($1, $2, $3, NULL, NULL, $6, $7, $8, NULL, NULL, $4, $5, $9, $10, $11) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_assvg(geom geometry, rel integer DEFAULT 0, maxdecimaldigits integer DEFAULT 15)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT COST 1000
AS '$libdir/postgis-2.5', $function$LWGEOM_asSVG$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_assvg(geog geography, rel integer DEFAULT 0, maxdecimaldigits integer DEFAULT 15)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_as_svg$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_assvg(text)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_AsSVG($1::public.geometry,0,15);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_astext(geometry, integer)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_asText$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_astext(text)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_AsText($1::public.geometry);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_astext(geography, integer)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_asText$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_astext(geometry)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT COST 750
AS '$libdir/postgis-2.5', $function$LWGEOM_asText$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_astext(geography)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_asText$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_astiff(rast raster, options text[] DEFAULT NULL::text[], srid integer DEFAULT NULL::integer)
 RETURNS bytea
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		i int;
		num_bands int;
		nodata double precision;
		last_nodata double precision;
	BEGIN
		IF rast IS NULL THEN
			RETURN NULL;
		END IF;

		num_bands := st_numbands($1);

		-- TIFF only allows one NODATA value for ALL bands
		FOR i IN 1..num_bands LOOP
			nodata := st_bandnodatavalue($1, i);
			IF last_nodata IS NULL THEN
				last_nodata := nodata;
			ELSEIF nodata != last_nodata THEN
				RAISE NOTICE 'The TIFF format only permits one NODATA value for all bands.  The value used will be the last band with a NODATA value.';
			END IF;
		END LOOP;

		RETURN st_asgdalraster($1, 'GTiff', $2, $3);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_astiff(rast raster, compression text, srid integer DEFAULT NULL::integer)
 RETURNS bytea
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		compression2 text;
		c_type text;
		c_level int;
		i int;
		num_bands int;
		options text[];
	BEGIN
		IF rast IS NULL THEN
			RETURN NULL;
		END IF;

		compression2 := trim(both from upper(compression));

		IF length(compression2) > 0 THEN
			-- JPEG
			IF position('JPEG' in compression2) != 0 THEN
				c_type := 'JPEG';
				c_level := substring(compression2 from '[0-9]+$');

				IF c_level IS NOT NULL THEN
					IF c_level > 100 THEN
						c_level := 100;
					ELSEIF c_level < 1 THEN
						c_level := 1;
					END IF;

					options := array_append(options, 'JPEG_QUALITY=' || c_level);
				END IF;

				-- per band pixel type check
				num_bands := st_numbands($1);
				FOR i IN 1..num_bands LOOP
					IF public.ST_BandPixelType($1, i) != '8BUI' THEN
						RAISE EXCEPTION 'The pixel type of band % in the raster is not 8BUI.  JPEG compression can only be used with the 8BUI pixel type.', i;
					END IF;
				END LOOP;

			-- DEFLATE
			ELSEIF position('DEFLATE' in compression2) != 0 THEN
				c_type := 'DEFLATE';
				c_level := substring(compression2 from '[0-9]+$');

				IF c_level IS NOT NULL THEN
					IF c_level > 9 THEN
						c_level := 9;
					ELSEIF c_level < 1 THEN
						c_level := 1;
					END IF;

					options := array_append(options, 'ZLEVEL=' || c_level);
				END IF;

			ELSE
				c_type := compression2;

				-- CCITT
				IF position('CCITT' in compression2) THEN
					-- per band pixel type check
					num_bands := st_numbands($1);
					FOR i IN 1..num_bands LOOP
						IF public.ST_BandPixelType($1, i) != '1BB' THEN
							RAISE EXCEPTION 'The pixel type of band % in the raster is not 1BB.  CCITT compression can only be used with the 1BB pixel type.', i;
						END IF;
					END LOOP;
				END IF;

			END IF;

			-- compression type check
			IF ARRAY[c_type] <@ ARRAY['JPEG', 'LZW', 'PACKBITS', 'DEFLATE', 'CCITTRLE', 'CCITTFAX3', 'CCITTFAX4', 'NONE'] THEN
				options := array_append(options, 'COMPRESS=' || c_type);
			ELSE
				RAISE NOTICE 'Unknown compression type: %.  The outputted TIFF will not be COMPRESSED.', c_type;
			END IF;
		END IF;

		RETURN st_astiff($1, options, $3);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_astiff(rast raster, nbands integer[], compression text, srid integer DEFAULT NULL::integer)
 RETURNS bytea
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT st_astiff(st_band($1, $2), $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_astiff(rast raster, nbands integer[], options text[] DEFAULT NULL::text[], srid integer DEFAULT NULL::integer)
 RETURNS bytea
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT st_astiff(st_band($1, $2), $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_astwkb(geom geometry[], ids bigint[], prec integer DEFAULT NULL::integer, prec_z integer DEFAULT NULL::integer, prec_m integer DEFAULT NULL::integer, with_sizes boolean DEFAULT NULL::boolean, with_boxes boolean DEFAULT NULL::boolean)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$TWKBFromLWGEOMArray$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_astwkb(geom geometry, prec integer DEFAULT NULL::integer, prec_z integer DEFAULT NULL::integer, prec_m integer DEFAULT NULL::integer, with_sizes boolean DEFAULT NULL::boolean, with_boxes boolean DEFAULT NULL::boolean)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$TWKBFromLWGEOM$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_aswkb(raster, outasin boolean DEFAULT false)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_asWKB$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_asx3d(geom geometry, maxdecimaldigits integer DEFAULT 15, options integer DEFAULT 0)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT public._ST_AsX3D(3,$1,$2,$3,'');$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_azimuth(geom1 geometry, geom2 geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_azimuth$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_azimuth(geog1 geography, geog2 geography)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$geography_azimuth$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_band(rast raster, nbands text, delimiter character DEFAULT ','::bpchar)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT  public.ST_band($1, regexp_split_to_array(regexp_replace($2, '[[:space:]]', '', 'g'), E'\\' || array_to_string(regexp_split_to_array($3, ''), E'\\'))::int[]) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_band(rast raster, nband integer)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT  public.ST_band($1, ARRAY[$2]) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_band(rast raster, nbands integer[] DEFAULT ARRAY[1])
 RETURNS raster
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_band$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_bandfilesize(rast raster, band integer DEFAULT 1)
 RETURNS bigint
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getBandFileSize$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_bandfiletimestamp(rast raster, band integer DEFAULT 1)
 RETURNS bigint
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getBandFileTimestamp$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_bandisnodata(rast raster, forcechecking boolean)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_bandisnodata($1, 1, $2) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_bandisnodata(rast raster, band integer DEFAULT 1, forcechecking boolean DEFAULT false)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_bandIsNoData$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_bandmetadata(rast raster, band integer DEFAULT 1, OUT pixeltype text, OUT nodatavalue double precision, OUT isoutdb boolean, OUT path text, OUT outdbbandnum integer, OUT filesize bigint, OUT filetimestamp bigint)
 RETURNS record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT pixeltype, nodatavalue, isoutdb, path, outdbbandnum, filesize, filetimestamp FROM public.ST_BandMetaData($1, ARRAY[$2]::int[]) LIMIT 1 $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_bandmetadata(rast raster, band integer[], OUT bandnum integer, OUT pixeltype text, OUT nodatavalue double precision, OUT isoutdb boolean, OUT path text, OUT outdbbandnum integer, OUT filesize bigint, OUT filetimestamp bigint)
 RETURNS record
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_bandmetadata$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_bandnodatavalue(rast raster, band integer DEFAULT 1)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getBandNoDataValue$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_bandpath(rast raster, band integer DEFAULT 1)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getBandPath$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_bandpixeltype(rast raster, band integer DEFAULT 1)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getBandPixelTypeName$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_bdmpolyfromtext(text, integer)
 RETURNS geometry
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
DECLARE
	geomtext alias for $1;
	srid alias for $2;
	mline public.geometry;
	geom public.geometry;
BEGIN
	mline := public.ST_MultiLineStringFromText(geomtext, srid);

	IF mline IS NULL
	THEN
		RAISE EXCEPTION 'Input is not a MultiLinestring';
	END IF;

	geom := public.ST_Multi(public.ST_BuildArea(mline));

	RETURN geom;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_bdpolyfromtext(text, integer)
 RETURNS geometry
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
DECLARE
	geomtext alias for $1;
	srid alias for $2;
	mline public.geometry;
	geom public.geometry;
BEGIN
	mline := public.ST_MultiLineStringFromText(geomtext, srid);

	IF mline IS NULL
	THEN
		RAISE EXCEPTION 'Input is not a MultiLinestring';
	END IF;

	geom := public.ST_BuildArea(mline);

	IF public.GeometryType(geom) != 'POLYGON'
	THEN
		RAISE EXCEPTION 'Input returns more then a single polygon, try using BdMPolyFromText instead';
	END IF;

	RETURN geom;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_boundary(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$boundary$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_boundingdiagonal(geom geometry, fits boolean DEFAULT false)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_BoundingDiagonal$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_box2dfromgeohash(text, integer DEFAULT NULL::integer)
 RETURNS box2d
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$box2d_from_geohash$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_buffer(geography, double precision, text)
 RETURNS geography
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.geography(public.ST_Transform(public.ST_Buffer(public.ST_Transform(public.geometry($1), public._ST_BestSRID($1)), $2, $3), 4326))$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_buffer(geometry, double precision, text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_Buffer($1, $2,
		CAST( regexp_replace($3, '^[0123456789]+$',
			'quad_segs='||$3) AS cstring)
		)
	   $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_buffer(geometry, double precision, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_Buffer($1, $2,
		CAST('quad_segs='||CAST($3 AS text) as cstring))
	   $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_buffer(text, double precision, text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_Buffer($1::public.geometry, $2, $3);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_buffer(text, double precision, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_Buffer($1::public.geometry, $2, $3);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_buffer(geometry, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$buffer$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_buffer(text, double precision)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_Buffer($1::public.geometry, $2);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_buffer(geography, double precision, integer)
 RETURNS geography
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.geography(public.ST_Transform(public.ST_Buffer(public.ST_Transform(public.geometry($1), public._ST_BestSRID($1)), $2, $3), 4326))$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_buffer(geography, double precision)
 RETURNS geography
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.geography(public.ST_Transform(public.ST_Buffer(public.ST_Transform(public.geometry($1), public._ST_BestSRID($1)), $2), 4326))$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_buildarea(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_BuildArea$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_centroid(geography, use_spheroid boolean DEFAULT true)
 RETURNS geography
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_centroid$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_centroid(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$centroid$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_centroid(text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_Centroid($1::public.geometry);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_chaikinsmoothing(geometry, integer DEFAULT 1, boolean DEFAULT false)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_ChaikinSmoothing$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_cleangeometry(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_CleanGeometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_clip(rast raster, nband integer, geom geometry, crop boolean)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_Clip($1, ARRAY[$2]::integer[], $3, null::double precision[], $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_clip(rast raster, geom geometry, nodataval double precision, crop boolean DEFAULT true)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_Clip($1, NULL, $2, ARRAY[$3]::double precision[], $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_clip(rast raster, nband integer[], geom geometry, nodataval double precision[] DEFAULT NULL::double precision[], crop boolean DEFAULT true)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	BEGIN
		-- short-cut if geometry's extent fully contains raster's extent
		IF (nodataval IS NULL OR array_length(nodataval, 1) < 1) AND public.ST_Contains(geom, public.ST_Envelope(rast)) THEN
			RETURN rast;
		END IF;

		RETURN public._ST_Clip($1, $2, $3, $4, $5);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_clip(rast raster, geom geometry, crop boolean)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_Clip($1, NULL, $2, null::double precision[], $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_clip(rast raster, nband integer, geom geometry, nodataval double precision, crop boolean DEFAULT true)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_Clip($1, ARRAY[$2]::integer[], $3, ARRAY[$4]::double precision[], $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_clip(rast raster, geom geometry, nodataval double precision[] DEFAULT NULL::double precision[], crop boolean DEFAULT true)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_Clip($1, NULL, $2, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_clipbybox2d(geom geometry, box box2d)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT COST 50
AS '$libdir/postgis-2.5', $function$ST_ClipByBox2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_closestpoint(geom1 geometry, geom2 geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_closestpoint$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_closestpointofapproach(geometry, geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_ClosestPointOfApproach$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_clusterdbscan(geometry, eps double precision, minpoints integer)
 RETURNS integer
 LANGUAGE c
 WINDOW IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_ClusterDBSCAN$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_clusterintersecting(geometry[])
 RETURNS geometry[]
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$clusterintersecting_garray$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_clusterkmeans(geom geometry, k integer)
 RETURNS integer
 LANGUAGE c
 WINDOW STRICT
AS '$libdir/postgis-2.5', $function$ST_ClusterKMeans$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_clusterwithin(geometry[], double precision)
 RETURNS geometry[]
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$cluster_within_distance_garray$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_collect(geometry[])
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_collect_garray$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_collect(geom1 geometry, geom2 geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$LWGEOM_collect$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_collectionextract(geometry, integer)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_CollectionExtract$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_collectionhomogenize(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_CollectionHomogenize$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_colormap(rast raster, colormap text, method text DEFAULT 'INTERPOLATE'::text)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_ColorMap($1, 1, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_colormap(rast raster, nband integer DEFAULT 1, colormap text DEFAULT 'grayscale'::text, method text DEFAULT 'INTERPOLATE'::text)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
	DECLARE
		_ismap boolean;
		_colormap text;
		_element text[];
	BEGIN
		_ismap := TRUE;

		-- clean colormap to see what it is
		_colormap := split_part(colormap, E'\n', 1);
		_colormap := regexp_replace(_colormap, E':+', ' ', 'g');
		_colormap := regexp_replace(_colormap, E',+', ' ', 'g');
		_colormap := regexp_replace(_colormap, E'\\t+', ' ', 'g');
		_colormap := regexp_replace(_colormap, E' +', ' ', 'g');
		_element := regexp_split_to_array(_colormap, ' ');

		-- treat as colormap
		IF (array_length(_element, 1) > 1) THEN
			_colormap := colormap;
		-- treat as keyword
		ELSE
			method := 'INTERPOLATE';
			CASE lower(trim(both from _colormap))
				WHEN 'grayscale', 'greyscale' THEN
					_colormap := '
100%   0
  0% 254
  nv 255
					';
				WHEN 'pseudocolor' THEN
					_colormap := '
100% 255   0   0 255
 50%   0 255   0 255
  0%   0   0 255 255
  nv   0   0   0   0
					';
				WHEN 'fire' THEN
					_colormap := '
  100% 243 255 221 255
93.75% 242 255 178 255
 87.5% 255 255 135 255
81.25% 255 228  96 255
   75% 255 187  53 255
68.75% 255 131   7 255
 62.5% 255  84   0 255
56.25% 255  42   0 255
   50% 255   0   0 255
43.75% 255  42   0 255
 37.5% 224  74   0 255
31.25% 183  91   0 255
   25% 140  93   0 255
18.75%  99  82   0 255
 12.5%  58  58   1 255
 6.25%  12  15   0 255
    0%   0   0   0 255
    nv   0   0   0   0
					';
				WHEN 'bluered' THEN
					_colormap := '
100.00% 165   0  33 255
 94.12% 216  21  47 255
 88.24% 247  39  53 255
 82.35% 255  61  61 255
 76.47% 255 120  86 255
 70.59% 255 172 117 255
 64.71% 255 214 153 255
 58.82% 255 241 188 255
 52.94% 255 255 234 255
 47.06% 234 255 255 255
 41.18% 188 249 255 255
 35.29% 153 234 255 255
 29.41% 117 211 255 255
 23.53%  86 176 255 255
 17.65%  61 135 255 255
 11.76%  40  87 255 255
  5.88%  24  28 247 255
  0.00%  36   0 216 255
     nv   0   0   0   0
					';
				ELSE
					RAISE EXCEPTION 'Unknown colormap keyword: %', colormap;
			END CASE;
		END IF;

		RETURN public._ST_colormap($1, $2, _colormap, $4);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_combine_bbox(box3d, geometry)
 RETURNS box3d
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._postgis_deprecate('ST_Combine_BBox', 'ST_CombineBbox', '2.2.0');
    SELECT public.ST_CombineBbox($1,$2);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_combine_bbox(box2d, geometry)
 RETURNS box2d
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._postgis_deprecate('ST_Combine_BBox', 'ST_CombineBbox', '2.2.0');
    SELECT public.ST_CombineBbox($1,$2);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_combinebbox(box2d, geometry)
 RETURNS box2d
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$BOX2D_combine$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_combinebbox(box3d, box3d)
 RETURNS box3d
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$BOX3D_combine_BOX3D$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_combinebbox(box3d, geometry)
 RETURNS box3d
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$BOX3D_combine$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_concavehull(param_geom geometry, param_pctconvex double precision, param_allow_holes boolean DEFAULT false)
 RETURNS geometry
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
	DECLARE
		var_convhull public.geometry := public.ST_ForceSFS(public.ST_ConvexHull(param_geom));
		var_param_geom public.geometry := public.ST_ForceSFS(param_geom);
		var_initarea float := public.ST_Area(var_convhull);
		var_newarea float := var_initarea;
		var_div integer := 6; 
		var_tempgeom public.geometry;
		var_tempgeom2 public.geometry;
		var_cent public.geometry;
		var_geoms public.geometry[4]; 
		var_enline public.geometry;
		var_resultgeom public.geometry;
		var_atempgeoms public.geometry[];
		var_buf float := 1; 
	BEGIN
		-- We start with convex hull as our base
		var_resultgeom := var_convhull;

		IF param_pctconvex = 1 THEN
			-- this is the same as asking for the convex hull
			return var_resultgeom;
		ELSIF public.ST_GeometryType(var_param_geom) = 'ST_Polygon' THEN -- it is as concave as it is going to get
			IF param_allow_holes THEN -- leave the holes
				RETURN var_param_geom;
			ELSE -- remove the holes
				var_resultgeom := public.ST_MakePolygon(public.ST_ExteriorRing(var_param_geom));
				RETURN var_resultgeom;
			END IF;
		END IF;
		IF public.ST_Dimension(var_resultgeom) > 1 AND param_pctconvex BETWEEN 0 and 0.99 THEN
		-- get linestring that forms envelope of geometry
			var_enline := public.ST_Boundary(public.ST_Envelope(var_param_geom));
			var_buf := public.ST_Length(var_enline)/1000.0;
			IF public.ST_GeometryType(var_param_geom) = 'ST_MultiPoint' AND public.ST_NumGeometries(var_param_geom) BETWEEN 4 and 200 THEN
			-- we make polygons out of points since they are easier to cave in.
			-- Note we limit to between 4 and 200 points because this process is slow and gets quadratically slow
				var_buf := sqrt(public.ST_Area(var_convhull)*0.8/(public.ST_NumGeometries(var_param_geom)*public.ST_NumGeometries(var_param_geom)));
				var_atempgeoms := ARRAY(SELECT geom FROM public.ST_DumpPoints(var_param_geom));
				-- 5 and 10 and just fudge factors
				var_tempgeom := public.ST_Union(ARRAY(SELECT geom
						FROM (
						-- fuse near neighbors together
						SELECT DISTINCT ON (i) i,  public.ST_Distance(var_atempgeoms[i],var_atempgeoms[j]), public.ST_Buffer(public.ST_MakeLine(var_atempgeoms[i], var_atempgeoms[j]) , var_buf*5, 'quad_segs=3') As geom
								FROM generate_series(1,array_upper(var_atempgeoms, 1)) As i
									INNER JOIN generate_series(1,array_upper(var_atempgeoms, 1)) As j
										ON (
								 NOT public.ST_Intersects(var_atempgeoms[i],var_atempgeoms[j])
									AND public.ST_DWithin(var_atempgeoms[i],var_atempgeoms[j], var_buf*10)
									)
								UNION ALL
						-- catch the ones with no near neighbors
								SELECT i, 0, public.ST_Buffer(var_atempgeoms[i] , var_buf*10, 'quad_segs=3') As geom
								FROM generate_series(1,array_upper(var_atempgeoms, 1)) As i
									LEFT JOIN generate_series(ceiling(array_upper(var_atempgeoms,1)/2)::integer,array_upper(var_atempgeoms, 1)) As j
										ON (
								 NOT public.ST_Intersects(var_atempgeoms[i],var_atempgeoms[j])
									AND public.ST_DWithin(var_atempgeoms[i],var_atempgeoms[j], var_buf*10)
									)
									WHERE j IS NULL
								ORDER BY 1, 2
							) As foo	) );
				IF public.ST_IsValid(var_tempgeom) AND public.ST_GeometryType(var_tempgeom) = 'ST_Polygon' THEN
					var_tempgeom := public.ST_ForceSFS(public.ST_Intersection(var_tempgeom, var_convhull));
					IF param_allow_holes THEN
						var_param_geom := var_tempgeom;
					ELSIF public.ST_GeometryType(var_tempgeom) = 'ST_Polygon' THEN
						var_param_geom := public.ST_ForceSFS(public.ST_MakePolygon(public.ST_ExteriorRing(var_tempgeom)));
					ELSE
						var_param_geom := public.ST_ForceSFS(public.ST_ConvexHull(var_param_geom));
					END IF;
					-- make sure result covers original (#3638)
					var_param_geom := public.ST_Union(param_geom, var_param_geom);
					return var_param_geom;
				ELSIF public.ST_IsValid(var_tempgeom) THEN
					var_param_geom := public.ST_ForceSFS(public.ST_Intersection(var_tempgeom, var_convhull));
				END IF;
			END IF;

			IF public.ST_GeometryType(var_param_geom) = 'ST_Polygon' THEN
				IF NOT param_allow_holes THEN
					var_param_geom := public.ST_ForceSFS(public.ST_MakePolygon(public.ST_ExteriorRing(var_param_geom)));
				END IF;
				-- make sure result covers original (#3638)
				--var_param_geom := public.ST_Union(param_geom, var_param_geom);
				return var_param_geom;
			END IF;
            var_cent := public.ST_Centroid(var_param_geom);
            IF (public.ST_XMax(var_enline) - public.ST_XMin(var_enline) ) > var_buf AND (public.ST_YMax(var_enline) - public.ST_YMin(var_enline) ) > var_buf THEN
                    IF public.ST_Dwithin(public.ST_Centroid(var_convhull) , public.ST_Centroid(public.ST_Envelope(var_param_geom)), var_buf/2) THEN
                -- If the geometric dimension is > 1 and the object is symettric (cutting at centroid will not work -- offset a bit)
                        var_cent := public.ST_Translate(var_cent, (public.ST_XMax(var_enline) - public.ST_XMin(var_enline))/1000,  (public.ST_YMAX(var_enline) - public.ST_YMin(var_enline))/1000);
                    ELSE
                        -- uses closest point on geometry to centroid. I can't explain why we are doing this
                        var_cent := public.ST_ClosestPoint(var_param_geom,var_cent);
                    END IF;
                    IF public.ST_DWithin(var_cent, var_enline,var_buf) THEN
                        var_cent := public.ST_centroid(public.ST_Envelope(var_param_geom));
                    END IF;
                    -- break envelope into 4 triangles about the centroid of the geometry and returned the clipped geometry in each quadrant
                    FOR i in 1 .. 4 LOOP
                       var_geoms[i] := public.ST_MakePolygon(public.ST_MakeLine(ARRAY[public.ST_PointN(var_enline,i), public.ST_PointN(var_enline,i+1), var_cent, public.ST_PointN(var_enline,i)]));
                       var_geoms[i] := public.ST_ForceSFS(public.ST_Intersection(var_param_geom, public.ST_Buffer(var_geoms[i],var_buf)));
                       IF public.ST_IsValid(var_geoms[i]) THEN

                       ELSE
                            var_geoms[i] := public.ST_BuildArea(public.ST_MakeLine(ARRAY[public.ST_PointN(var_enline,i), public.ST_PointN(var_enline,i+1), var_cent, public.ST_PointN(var_enline,i)]));
                       END IF;
                    END LOOP;
                    var_tempgeom := public.ST_Union(ARRAY[public.ST_ConvexHull(var_geoms[1]), public.ST_ConvexHull(var_geoms[2]) , public.ST_ConvexHull(var_geoms[3]), public.ST_ConvexHull(var_geoms[4])]);
                    --RAISE NOTICE 'Curr vex % ', public.ST_AsText(var_tempgeom);
                    IF public.ST_Area(var_tempgeom) <= var_newarea AND public.ST_IsValid(var_tempgeom)  THEN --AND public.ST_GeometryType(var_tempgeom) ILIKE '%Polygon'

                        var_tempgeom := public.ST_Buffer(public.ST_ConcaveHull(var_geoms[1],least(param_pctconvex + param_pctconvex/var_div),true),var_buf, 'quad_segs=2');
                        FOR i IN 1 .. 4 LOOP
                            var_geoms[i] := public.ST_Buffer(public.ST_ConcaveHull(var_geoms[i],least(param_pctconvex + param_pctconvex/var_div),true), var_buf, 'quad_segs=2');
                            IF public.ST_IsValid(var_geoms[i]) Then
                                var_tempgeom := public.ST_Union(var_tempgeom, var_geoms[i]);
                            ELSE
                                RAISE NOTICE 'Not valid % %', i, public.ST_AsText(var_tempgeom);
                                var_tempgeom := public.ST_Union(var_tempgeom, public.ST_ConvexHull(var_geoms[i]));
                            END IF;
                        END LOOP;

                        --RAISE NOTICE 'Curr concave % ', public.ST_AsText(var_tempgeom);
                        IF public.ST_IsValid(var_tempgeom) THEN
                            var_resultgeom := var_tempgeom;
                        END IF;
                        var_newarea := public.ST_Area(var_resultgeom);
                    ELSIF public.ST_IsValid(var_tempgeom) THEN
                        var_resultgeom := var_tempgeom;
                    END IF;

                    IF public.ST_NumGeometries(var_resultgeom) > 1  THEN
                        var_tempgeom := public._ST_ConcaveHull(var_resultgeom);
                        IF public.ST_IsValid(var_tempgeom) AND public.ST_GeometryType(var_tempgeom) ILIKE 'ST_Polygon' THEN
                            var_resultgeom := var_tempgeom;
                        ELSE
                            var_resultgeom := public.ST_Buffer(var_tempgeom,var_buf, 'quad_segs=2');
                        END IF;
                    END IF;
                    IF param_allow_holes = false THEN
                    -- only keep exterior ring since we do not want holes
                        var_resultgeom := public.ST_MakePolygon(public.ST_ExteriorRing(var_resultgeom));
                    END IF;
                ELSE
                    var_resultgeom := public.ST_Buffer(var_resultgeom,var_buf);
                END IF;
                var_resultgeom := public.ST_ForceSFS(public.ST_Intersection(var_resultgeom, public.ST_ConvexHull(var_param_geom)));
            ELSE
                -- dimensions are too small to cut
                var_resultgeom := public._ST_ConcaveHull(var_param_geom);
            END IF;

            RETURN var_resultgeom;
	END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_contains(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.~) $2 AND public._ST_Contains($1,$2)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_contains(rast1 raster, nband1 integer, rast2 raster, nband2 integer)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT $1 OPERATOR(public.&&) $3 AND CASE WHEN $2 IS NULL OR $4 IS NULL THEN public._st_contains(public.st_convexhull($1), public.st_convexhull($3)) ELSE public._st_contains($1, $2, $3, $4) END $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_contains(rast1 raster, rast2 raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT public.st_contains($1, NULL::integer, $2, NULL::integer) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_containsproperly(rast1 raster, nband1 integer, rast2 raster, nband2 integer)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT $1 OPERATOR(public.&&) $3 AND CASE WHEN $2 IS NULL OR $4 IS NULL THEN public._st_containsproperly(public.st_convexhull($1), public.st_convexhull($3)) ELSE public._st_containsproperly($1, $2, $3, $4) END $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_containsproperly(rast1 raster, rast2 raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT public.st_containsproperly($1, NULL::integer, $2, NULL::integer) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_containsproperly(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.~) $2 AND public._ST_ContainsProperly($1,$2)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_convexhull(raster)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT COST 300
AS '$libdir/rtpostgis-2.5', $function$RASTER_convex_hull$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_convexhull(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$convexhull$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_coorddim(geometry geometry)
 RETURNS smallint
 LANGUAGE c
 IMMUTABLE STRICT COST 5
AS '$libdir/postgis-2.5', $function$LWGEOM_ndims$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_count(rast raster, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true)
 RETURNS bigint
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_count($1, $2, $3, 1) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_count(rast raster, exclude_nodata_value boolean)
 RETURNS bigint
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_count($1, 1, $2, 1) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_count(rastertable text, rastercolumn text, exclude_nodata_value boolean)
 RETURNS bigint
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_count($1, $2, 1, $3, 1) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_count(rastertable text, rastercolumn text, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true)
 RETURNS bigint
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_count($1, $2, $3, $4, 1) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_coveredby(text, text)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_CoveredBy($1::public.geometry, $2::public.geometry);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_coveredby(geography, geography)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.&&) $2 AND public._ST_Covers($2, $1)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_coveredby(rast1 raster, rast2 raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT public.st_coveredby($1, NULL::integer, $2, NULL::integer) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_coveredby(rast1 raster, nband1 integer, rast2 raster, nband2 integer)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT $1 OPERATOR(public.&&) $3 AND CASE WHEN $2 IS NULL OR $4 IS NULL THEN public._st_coveredby(public.st_convexhull($1), public.st_convexhull($3)) ELSE public._st_coveredby($1, $2, $3, $4) END $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_coveredby(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.@) $2 AND public._ST_CoveredBy($1,$2)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_covers(text, text)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_Covers($1::public.geometry, $2::public.geometry);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_covers(rast1 raster, rast2 raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT public.st_covers($1, NULL::integer, $2, NULL::integer) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_covers(geography, geography)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.&&) $2 AND public._ST_Covers($1, $2)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_covers(rast1 raster, nband1 integer, rast2 raster, nband2 integer)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT $1 OPERATOR(public.&&) $3 AND CASE WHEN $2 IS NULL OR $4 IS NULL THEN public._st_covers(public.st_convexhull($1), public.st_convexhull($3)) ELSE public._st_covers($1, $2, $3, $4) END $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_covers(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.~) $2 AND public._ST_Covers($1,$2)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_cpawithin(geometry, geometry, double precision)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_CPAWithin$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_createoverview(tab regclass, col name, factor integer, algo text DEFAULT 'NearestNeighbour'::text)
 RETURNS regclass
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
  sinfo RECORD; -- source info
  sql TEXT;
  ttab TEXT;
BEGIN

  -- 0. Check arguments, we need to ensure:
  --    a. Source table has a raster column with given name
  --    b. Source table has a fixed scale (or "factor" would have no meaning)
  --    c. Source table has a known extent ? (we could actually compute it)
  --    d. Source table has a fixed tile size (or "factor" would have no meaning?)
  -- # all of the above can be checked with a query to raster_columns
  sql := 'SELECT r.r_table_schema sch, r.r_table_name tab, '
      || 'r.scale_x sfx, r.scale_y sfy, r.blocksize_x tw, '
      || 'r.blocksize_y th, r.extent ext, r.srid FROM public.raster_columns r, '
      || 'pg_class c, pg_namespace n WHERE r.r_table_schema = n.nspname '
      || 'AND r.r_table_name = c.relname AND r_raster_column = $2 AND '
      || ' c.relnamespace = n.oid AND c.oid = $1'
  ;
  EXECUTE sql INTO sinfo USING tab, col;
  IF sinfo IS NULL THEN
      RAISE EXCEPTION '%.% raster column does not exist', tab::text, col;
  END IF;
  IF sinfo.sfx IS NULL or sinfo.sfy IS NULL THEN
    RAISE EXCEPTION 'cannot create overview without scale constraint, try select AddRasterConstraints(''%'', ''%'');', tab::text, col;
  END IF;
  IF sinfo.tw IS NULL or sinfo.tw IS NULL THEN
    RAISE EXCEPTION 'cannot create overview without tilesize constraint, try select AddRasterConstraints(''%'', ''%'');', tab::text, col;
  END IF;
  IF sinfo.ext IS NULL THEN
    RAISE EXCEPTION 'cannot create overview without extent constraint, try select AddRasterConstraints(''%'', ''%'');', tab::text, col;
  END IF;

  -- TODO: lookup in raster_overviews to see if there's any
  --       lower-resolution table to start from

  ttab := 'o_' || factor || '_' || sinfo.tab;
  sql := 'CREATE TABLE ' || quote_ident(sinfo.sch)
      || '.' || quote_ident(ttab)
      || ' AS SELECT public.ST_Retile($1, $2, $3, $4, $5, $6, $7) '
      || quote_ident(col);
  EXECUTE sql USING tab, col, sinfo.ext,
                    sinfo.sfx * factor, sinfo.sfy * factor,
                    sinfo.tw, sinfo.th, algo;

  -- TODO: optimize this using knowledge we have about
  --       the characteristics of the target column ?
  PERFORM public.AddRasterConstraints(sinfo.sch, ttab, col);

  PERFORM  public.AddOverviewConstraints(sinfo.sch, ttab, col,
                                 sinfo.sch, sinfo.tab, col, factor);

    -- return the schema as well as the table
  RETURN sinfo.sch||'.'||ttab;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_crosses(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.&&) $2 AND public._ST_Crosses($1,$2)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_curvetoline(geom geometry, tol double precision DEFAULT 32, toltype integer DEFAULT 0, flags integer DEFAULT 0)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_CurveToLine$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_delaunaytriangles(g1 geometry, tolerance double precision DEFAULT 0.0, flags integer DEFAULT 0)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_DelaunayTriangles$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_dfullywithin(rast1 raster, nband1 integer, rast2 raster, nband2 integer, distance double precision)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT $1::public.geometry OPERATOR(public.&&) public.ST_Expand(public.ST_ConvexHull($3), $5) AND $3::public.geometry OPERATOR(public.&&) public.ST_Expand(public.ST_ConvexHull($1), $5) AND CASE WHEN $2 IS NULL OR $4 IS NULL THEN public._ST_DFullyWithin(public.ST_ConvexHull($1), public.ST_Convexhull($3), $5) ELSE public._ST_DFullyWithin($1, $2, $3, $4, $5) END $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_dfullywithin(geom1 geometry, geom2 geometry, double precision)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.&&) public.ST_Expand($2,$3) AND $2 OPERATOR(public.&&) public.ST_Expand($1,$3) AND public._ST_DFullyWithin(public.ST_ConvexHull($1), public.ST_ConvexHull($2), $3)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_dfullywithin(rast1 raster, rast2 raster, distance double precision)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT public.ST_DFullyWithin($1, NULL::integer, $2, NULL::integer, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_difference(geom1 geometry, geom2 geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$difference$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_dimension(geometry)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$LWGEOM_dimension$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_disjoint(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$disjoint$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_disjoint(rast1 raster, nband1 integer, rast2 raster, nband2 integer)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT CASE WHEN $2 IS NULL OR $4 IS NULL THEN public.ST_Disjoint(public.ST_ConvexHull($1), public.ST_ConvexHull($3)) ELSE NOT public._ST_intersects($1, $2, $3, $4) END $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_disjoint(rast1 raster, rast2 raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT public.ST_Disjoint($1, NULL::integer, $2, NULL::integer) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_distance(geom1 geometry, geom2 geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 25
AS '$libdir/postgis-2.5', $function$distance$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_distance(text, text)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_Distance($1::public.geometry, $2::public.geometry);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_distance(geography, geography)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public._ST_Distance($1, $2, 0.0, true)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_distance(geography, geography, boolean)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public._ST_Distance($1, $2, 0.0, $3)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_distance_sphere(geom1 geometry, geom2 geometry)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT COST 300
AS $function$ SELECT public._postgis_deprecate('ST_Distance_Sphere', 'ST_DistanceSphere', '2.2.0');
    SELECT public.ST_DistanceSphere($1,$2);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_distance_spheroid(geom1 geometry, geom2 geometry, spheroid)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Distance_Spheroid', 'ST_DistanceSpheroid', '2.2.0');
    SELECT public.ST_DistanceSpheroid($1,$2,$3);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_distancecpa(geometry, geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_DistanceCPA$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_distancesphere(geom1 geometry, geom2 geometry)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT COST 300
AS $function$
	select public.ST_distance( public.geography($1), public.geography($2),false)
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_distancespheroid(geom1 geometry, geom2 geometry, spheroid)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 200
AS '$libdir/postgis-2.5', $function$LWGEOM_distance_ellipsoid$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_distinct4ma(matrix double precision[], nodatamode text, VARIADIC args text[])
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT COUNT(DISTINCT unnest)::float FROM unnest($1) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_distinct4ma(value double precision[], pos integer[], VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT COUNT(DISTINCT unnest)::double precision FROM unnest($1) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_dump(geometry)
 RETURNS SETOF geometry_dump
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$LWGEOM_dump$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_dumpaspolygons(rast raster, band integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true)
 RETURNS SETOF geomval
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_dumpAsPolygons$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_dumppoints(geometry)
 RETURNS SETOF geometry_dump
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$LWGEOM_dumppoints$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_dumprings(geometry)
 RETURNS SETOF geometry_dump
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_dump_rings$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_dumpvalues(rast raster, nband integer, exclude_nodata_value boolean DEFAULT true)
 RETURNS double precision[]
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT valarray FROM public.ST_dumpvalues($1, ARRAY[$2]::integer[], $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_dumpvalues(rast raster, nband integer[] DEFAULT NULL::integer[], exclude_nodata_value boolean DEFAULT true, OUT nband integer, OUT valarray double precision[])
 RETURNS SETOF record
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_dumpValues$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_dwithin(text, text, double precision)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_DWithin($1::public.geometry, $2::public.geometry, $3);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_dwithin(geom1 geometry, geom2 geometry, double precision)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.&&) public.ST_Expand($2,$3) AND $2 OPERATOR(public.&&) public.ST_Expand($1,$3) AND public._ST_DWithin($1, $2, $3)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_dwithin(rast1 raster, nband1 integer, rast2 raster, nband2 integer, distance double precision)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT $1::public.geometry OPERATOR(public.&&) public.ST_Expand(public.ST_ConvexHull($3), $5) AND $3::public.geometry OPERATOR(public.&&) public.ST_Expand(public.ST_ConvexHull($1), $5) AND CASE WHEN $2 IS NULL OR $4 IS NULL THEN public._ST_dwithin(public.st_convexhull($1), public.st_convexhull($3), $5) ELSE public._ST_dwithin($1, $2, $3, $4, $5) END $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_dwithin(geography, geography, double precision, boolean)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.&&) public._ST_Expand($2,$3) AND $2 OPERATOR(public.&&) public._ST_Expand($1,$3) AND public._ST_DWithin($1, $2, $3, $4)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_dwithin(rast1 raster, rast2 raster, distance double precision)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT public.st_dwithin($1, NULL::integer, $2, NULL::integer, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_dwithin(geography, geography, double precision)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.&&) public._ST_Expand($2,$3) AND $2 OPERATOR(public.&&) public._ST_Expand($1,$3) AND public._ST_DWithin($1, $2, $3, true)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_endpoint(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_endpoint_linestring$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_envelope(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_envelope$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_envelope(raster)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_envelope$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_equals(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.~=) $2 AND public._ST_Equals($1,$2)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_estimated_extent(text, text, text)
 RETURNS box2d
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Estimated_Extent', 'ST_EstimatedExtent', '2.1.0');
    -- We use security invoker instead of security definer
    -- to prevent malicious injection of a different same named function
    SELECT public.ST_EstimatedExtent($1, $2, $3);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_estimated_extent(text, text)
 RETURNS box2d
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Estimated_Extent', 'ST_EstimatedExtent', '2.1.0');
    -- We use security invoker instead of security definer
    -- to prevent malicious injection of a same named different function
    -- that would be run under elevated permissions
    SELECT public.ST_EstimatedExtent($1, $2);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_estimatedextent(text, text, text, boolean)
 RETURNS box2d
 LANGUAGE c
 IMMUTABLE STRICT SECURITY DEFINER
AS '$libdir/postgis-2.5', $function$gserialized_estimated_extent$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_estimatedextent(text, text)
 RETURNS box2d
 LANGUAGE c
 IMMUTABLE STRICT SECURITY DEFINER
AS '$libdir/postgis-2.5', $function$gserialized_estimated_extent$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_estimatedextent(text, text, text)
 RETURNS box2d
 LANGUAGE c
 IMMUTABLE STRICT SECURITY DEFINER
AS '$libdir/postgis-2.5', $function$gserialized_estimated_extent$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_expand(geom geometry, dx double precision, dy double precision, dz double precision DEFAULT 0, dm double precision DEFAULT 0)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_expand$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_expand(geometry, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_expand$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_expand(box2d, double precision)
 RETURNS box2d
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX2D_expand$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_expand(box3d, double precision)
 RETURNS box3d
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX3D_expand$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_expand(box box3d, dx double precision, dy double precision, dz double precision DEFAULT 0)
 RETURNS box3d
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX3D_expand$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_expand(box box2d, dx double precision, dy double precision)
 RETURNS box2d
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX2D_expand$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_exteriorring(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_exteriorring_polygon$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_filterbym(geometry, double precision, double precision DEFAULT NULL::double precision, boolean DEFAULT false)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$LWGEOM_FilterByM$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_find_extent(text, text)
 RETURNS box2d
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Find_Extent', 'ST_FindExtent', '2.2.0');
    SELECT public.ST_FindExtent($1,$2);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_find_extent(text, text, text)
 RETURNS box2d
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Find_Extent', 'ST_FindExtent', '2.2.0');
    SELECT public.ST_FindExtent($1,$2,$3);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_findextent(text, text)
 RETURNS box2d
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
DECLARE
	tablename alias for $1;
	columnname alias for $2;
	myrec RECORD;

BEGIN
	FOR myrec IN EXECUTE 'SELECT public.ST_Extent("' || columnname || '") As extent FROM "' || tablename || '"' LOOP
		return myrec.extent;
	END LOOP;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_findextent(text, text, text)
 RETURNS box2d
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
DECLARE
	schemaname alias for $1;
	tablename alias for $2;
	columnname alias for $3;
	myrec RECORD;
BEGIN
	FOR myrec IN EXECUTE 'SELECT public.ST_Extent("' || columnname || '") As extent FROM "' || schemaname || '"."' || tablename || '"' LOOP
		return myrec.extent;
	END LOOP;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_flipcoordinates(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_FlipCoordinates$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_force2d(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_force_2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_force3d(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_force_3dz$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_force3dm(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_force_3dm$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_force3dz(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_force_3dz$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_force4d(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_force_4d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_force_2d(geometry)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Force_2d', 'ST_Force2D', '2.1.0');
    SELECT public.ST_Force2D($1);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_force_3d(geometry)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Force_3d', 'ST_Force3D', '2.1.0');
    SELECT public.ST_Force3D($1);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_force_3dm(geometry)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Force_3dm', 'ST_Force3DM', '2.1.0');
    SELECT public.ST_Force3DM($1);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_force_3dz(geometry)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Force_3dz', 'ST_Force3DZ', '2.1.0');
    SELECT public.ST_Force3DZ($1);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_force_4d(geometry)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Force_4d', 'ST_Force4D', '2.1.0');
    SELECT public.ST_Force4D($1);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_force_collection(geometry)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Force_Collection', 'ST_ForceCollection', '2.1.0');
    SELECT public.ST_ForceCollection($1);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_forcecollection(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_force_collection$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_forcecurve(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_force_curve$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_forcepolygonccw(geometry)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT COST 15
AS $function$ SELECT public.ST_Reverse(public.ST_ForcePolygonCW($1)) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_forcepolygoncw(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT COST 15
AS '$libdir/postgis-2.5', $function$LWGEOM_force_clockwise_poly$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_forcerhr(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_force_clockwise_poly$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_forcesfs(geometry, version text)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_force_sfs$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_forcesfs(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_force_sfs$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_frechetdistance(geom1 geometry, geom2 geometry, double precision DEFAULT '-1'::integer)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$ST_FrechetDistance$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_fromgdalraster(gdaldata bytea, srid integer DEFAULT NULL::integer)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_fromGDALRaster$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_gdaldrivers(OUT idx integer, OUT short_name text, OUT long_name text, OUT can_read boolean, OUT can_write boolean, OUT create_options text)
 RETURNS SETOF record
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getGDALDrivers$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_generatepoints(area geometry, npoints numeric)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_GeneratePoints$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geogfromtext(text)
 RETURNS geography
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_from_text$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geogfromwkb(bytea)
 RETURNS geography
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_from_binary$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geographyfromtext(text)
 RETURNS geography
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geography_from_text$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geohash(geog geography, maxchars integer DEFAULT 0)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_GeoHash$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geohash(geom geometry, maxchars integer DEFAULT 0)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_GeoHash$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geomcollfromtext(text, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE
	WHEN public.geometrytype(public.ST_GeomFromText($1, $2)) = 'GEOMETRYCOLLECTION'
	THEN public.ST_GeomFromText($1,$2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geomcollfromtext(text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE
	WHEN public.geometrytype(public.ST_GeomFromText($1)) = 'GEOMETRYCOLLECTION'
	THEN public.ST_GeomFromText($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geomcollfromwkb(bytea)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE
	WHEN public.geometrytype(public.ST_GeomFromWKB($1)) = 'GEOMETRYCOLLECTION'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geomcollfromwkb(bytea, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE
	WHEN public.geometrytype(public.ST_GeomFromWKB($1, $2)) = 'GEOMETRYCOLLECTION'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geometricmedian(g geometry, tolerance double precision DEFAULT NULL::double precision, max_iter integer DEFAULT 10000, fail_if_not_converged boolean DEFAULT false)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$ST_GeometricMedian$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geometryfromtext(text)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_from_text$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geometryfromtext(text, integer)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_from_text$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geometryn(geometry, integer)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_geometryn_collection$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geometrytype(geometry)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$geometry_geometrytype$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geomfromewkb(bytea)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOMFromEWKB$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geomfromewkt(text)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$parse_WKT_lwgeom$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geomfromgeohash(text, integer DEFAULT NULL::integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT CAST(public.ST_Box2dFromGeoHash($1, $2) AS geometry); $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geomfromgeojson(text)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geom_from_geojson$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geomfromgeojson(jsonb)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_GeomFromGeoJson($1::text)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geomfromgeojson(json)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_GeomFromGeoJson($1::text)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geomfromgml(text, integer)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geom_from_gml$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geomfromgml(text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public._ST_GeomFromGML($1, 0)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geomfromkml(text)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geom_from_kml$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geomfromtext(text)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_from_text$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geomfromtext(text, integer)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_from_text$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geomfromtwkb(bytea)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOMFromTWKB$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geomfromwkb(bytea)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_from_WKB$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geomfromwkb(bytea, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_SetSRID(public.ST_GeomFromWKB($1), $2)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_georeference(rast raster, format text DEFAULT 'GDAL'::text)
 RETURNS text
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
    DECLARE
				scale_x numeric;
				scale_y numeric;
				skew_x numeric;
				skew_y numeric;
				ul_x numeric;
				ul_y numeric;

        result text;
    BEGIN
			SELECT scalex::numeric, scaley::numeric, skewx::numeric, skewy::numeric, upperleftx::numeric, upperlefty::numeric
				INTO scale_x, scale_y, skew_x, skew_y, ul_x, ul_y FROM public.ST_Metadata(rast);

						-- scale x
            result := trunc(scale_x, 10) || E'\n';

						-- skew y
            result := result || trunc(skew_y, 10) || E'\n';

						-- skew x
            result := result || trunc(skew_x, 10) || E'\n';

						-- scale y
            result := result || trunc(scale_y, 10) || E'\n';

        IF format = 'ESRI' THEN
						-- upper left x
            result := result || trunc((ul_x + scale_x * 0.5), 10) || E'\n';

						-- upper left y
            result = result || trunc((ul_y + scale_y * 0.5), 10) || E'\n';
        ELSE -- IF format = 'GDAL' THEN
						-- upper left x
            result := result || trunc(ul_x, 10) || E'\n';

						-- upper left y
            result := result || trunc(ul_y, 10) || E'\n';
        END IF;

        RETURN result;
    END;
    $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_geotransform(raster, OUT imag double precision, OUT jmag double precision, OUT theta_i double precision, OUT theta_ij double precision, OUT xoffset double precision, OUT yoffset double precision)
 RETURNS record
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_getGeotransform$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_gmltosql(text, integer)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geom_from_gml$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_gmltosql(text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public._ST_GeomFromGML($1, 0)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_grayscale(rast raster, redband integer DEFAULT 1, greenband integer DEFAULT 2, blueband integer DEFAULT 3, extenttype text DEFAULT 'INTERSECTION'::text)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
	BEGIN

		RETURN public.ST_Grayscale(
			ARRAY[
				ROW(rast, redband)::rastbandarg,
				ROW(rast, greenband)::rastbandarg,
				ROW(rast, blueband)::rastbandarg
			]::rastbandarg[],
			extenttype
		);

	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_grayscale(rastbandargset rastbandarg[], extenttype text DEFAULT 'INTERSECTION'::text)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE

		_NBANDS integer DEFAULT 3;
		_NODATA integer DEFAULT 255;
		_PIXTYPE text DEFAULT '8BUI';

		_set rastbandarg[];

		nrast integer;
		idx integer;
		rast public.raster;
		nband integer;

		stats summarystats;
		nodata double precision;
		nodataval integer;
		reclassexpr text;

	BEGIN

		-- check for three rastbandarg
		nrast := array_length(rastbandargset, 1);
		IF nrast < _NBANDS THEN
			RAISE EXCEPTION '''rastbandargset'' must have three bands for red, green and blue';
		ELSIF nrast > _NBANDS THEN
			RAISE WARNING 'Only the first three elements of ''rastbandargset'' will be used';
			_set := rastbandargset[1:3];
		ELSE
			_set := rastbandargset;
		END IF;

		FOR idx IN 1.._NBANDS LOOP

			rast := _set[idx].rast;
			nband := _set[idx].nband;

			-- check that each raster has the specified band
			IF public.ST_HasNoBand(rast, nband) THEN

				RAISE EXCEPTION 'Band at index ''%'' not found for raster ''%''', nband, idx;

			-- check that each band is 8BUI. if not, reclassify to 8BUI
			ELSIF public.ST_BandPixelType(rast, nband) != _PIXTYPE THEN

				stats := public.ST_SummaryStats(rast, nband);
				nodata := public.ST_BandNoDataValue(rast, nband);

				IF nodata IS NOT NULL THEN
					nodataval := _NODATA;
					reclassexpr := concat(
						concat('[', nodata , '-', nodata, ']:', _NODATA, '-', _NODATA, ','),
						concat('[', stats.min , '-', stats.max , ']:0-', _NODATA - 1)
					);
				ELSE
					nodataval := NULL;
					reclassexpr := concat('[', stats.min , '-', stats.max , ']:0-', _NODATA);
				END IF;

				_set[idx] := ROW(
					public.ST_Reclass(
						rast,
						ROW(nband, reclassexpr, _PIXTYPE, nodataval)::reclassarg
					),
					nband
				)::rastbandarg;

			END IF;

		END LOOP;

		-- call map algebra with _st_grayscale4ma
		RETURN public.ST_MapAlgebra(
			_set,
			'public._ST_Grayscale4MA(double precision[][][], integer[][], text[])'::regprocedure,
			'8BUI',
			extenttype
		);

	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_hasarc(geometry geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_has_arc$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_hasnoband(rast raster, nband integer DEFAULT 1)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_hasNoBand$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_hausdorffdistance(geom1 geometry, geom2 geometry, double precision)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$hausdorffdistancedensify$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_hausdorffdistance(geom1 geometry, geom2 geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$hausdorffdistance$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_height(raster)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getHeight$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_hillshade(rast raster, nband integer DEFAULT 1, pixeltype text DEFAULT '32BF'::text, azimuth double precision DEFAULT 315.0, altitude double precision DEFAULT 45.0, max_bright double precision DEFAULT 255.0, scale double precision DEFAULT 1.0, interpolate_nodata boolean DEFAULT false)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_hillshade($1, $2, NULL::public.raster, $3, $4, $5, $6, $7, $8) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_hillshade(rast raster, nband integer, customextent raster, pixeltype text DEFAULT '32BF'::text, azimuth double precision DEFAULT 315.0, altitude double precision DEFAULT 45.0, max_bright double precision DEFAULT 255.0, scale double precision DEFAULT 1.0, interpolate_nodata boolean DEFAULT false)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		_rast public.raster;
		_nband integer;
		_pixtype text;
		_pixwidth double precision;
		_pixheight double precision;
		_width integer;
		_height integer;
		_customextent public.raster;
		_extenttype text;
	BEGIN
		_customextent := customextent;
		IF _customextent IS NULL THEN
			_extenttype := 'FIRST';
		ELSE
			_extenttype := 'CUSTOM';
		END IF;

		IF interpolate_nodata IS TRUE THEN
			_rast := public.ST_MapAlgebra(
				ARRAY[ROW(rast, nband)]::rastbandarg[],
				'public.st_invdistweight4ma(double precision[][][], integer[][], text[])'::regprocedure,
				pixeltype,
				'FIRST', NULL,
				1, 1
			);
			_nband := 1;
			_pixtype := NULL;
		ELSE
			_rast := rast;
			_nband := nband;
			_pixtype := pixeltype;
		END IF;

		-- get properties
		_pixwidth := public.ST_PixelWidth(_rast);
		_pixheight := public.ST_PixelHeight(_rast);
		SELECT width, height, scalex INTO _width, _height FROM public.ST_Metadata(_rast);

		RETURN public.ST_MapAlgebra(
			ARRAY[ROW(_rast, _nband)]::rastbandarg[],
			' public._ST_hillshade4ma(double precision[][][], integer[][], text[])'::regprocedure,
			_pixtype,
			_extenttype, _customextent,
			1, 1,
			_pixwidth::text, _pixheight::text,
			_width::text, _height::text,
			$5::text, $6::text,
			$7::text, $8::text
		);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_histogram(rastertable text, rastercolumn text, nband integer, bins integer, "right" boolean, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_histogram($1, $2, $3, TRUE, 1, $4, NULL, $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_histogram(rastertable text, rastercolumn text, nband integer, exclude_nodata_value boolean, bins integer, "right" boolean, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_histogram($1, $2, $3, $4, 1, $5, NULL, $6) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_histogram(rastertable text, rastercolumn text, nband integer, bins integer, width double precision[] DEFAULT NULL::double precision[], "right" boolean DEFAULT false, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE
AS $function$ SELECT public._ST_histogram($1, $2, $3, TRUE, 1, $4, $5, $6) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_histogram(rast raster, nband integer, bins integer, "right" boolean, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT min, max, count, percent FROM public._ST_histogram($1, $2, TRUE, 1, $3, NULL, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_histogram(rastertable text, rastercolumn text, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, bins integer DEFAULT 0, width double precision[] DEFAULT NULL::double precision[], "right" boolean DEFAULT false, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE
AS $function$ SELECT public._ST_histogram($1, $2, $3, $4, 1, $5, $6, $7) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_histogram(rast raster, nband integer, bins integer, width double precision[] DEFAULT NULL::double precision[], "right" boolean DEFAULT false, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT min, max, count, percent FROM public._ST_histogram($1, $2, TRUE, 1, $3, $4, $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_histogram(rast raster, nband integer, exclude_nodata_value boolean, bins integer, "right" boolean, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT min, max, count, percent FROM public._ST_histogram($1, $2, $3, 1, $4, NULL, $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_histogram(rast raster, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, bins integer DEFAULT 0, width double precision[] DEFAULT NULL::double precision[], "right" boolean DEFAULT false, OUT min double precision, OUT max double precision, OUT count bigint, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT min, max, count, percent FROM public._ST_histogram($1, $2, $3, 1, $4, $5, $6) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_interiorringn(geometry, integer)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_interiorringn_polygon$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_interpolatepoint(line geometry, point geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_InterpolatePoint$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersection(rast1 raster, band1 integer, rast2 raster, band2 integer, nodataval double precision[])
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.st_intersection($1, $2, $3, $4, 'BOTH', $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersection(rast1 raster, band1 integer, rast2 raster, band2 integer, nodataval double precision)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.st_intersection($1, $2, $3, $4, 'BOTH', ARRAY[$5, $5]) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersection(rast raster, geomin geometry)
 RETURNS SETOF geomval
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_Intersection($2, $1, 1) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersection(rast1 raster, band1 integer, rast2 raster, band2 integer, returnband text, nodataval double precision)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.st_intersection($1, $2, $3, $4, $5, ARRAY[$6, $6]) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersection(rast1 raster, rast2 raster, returnband text DEFAULT 'BOTH'::text, nodataval double precision[] DEFAULT NULL::double precision[])
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.st_intersection($1, 1, $2, 1, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersection(rast1 raster, rast2 raster, nodataval double precision[])
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.st_intersection($1, 1, $2, 1, 'BOTH', $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersection(rast raster, band integer, geomin geometry)
 RETURNS SETOF geomval
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_Intersection($3, $1, $2) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersection(geomin geometry, rast raster, band integer DEFAULT 1)
 RETURNS SETOF geomval
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
	DECLARE
		intersects boolean := FALSE;
	BEGIN
		intersects := public.ST_Intersects(geomin, rast, band);
		IF intersects THEN
			-- Return the intersections of the geometry with the vectorized parts of
			-- the raster and the values associated with those parts, if really their
			-- intersection is not empty.
			RETURN QUERY
				SELECT
					intgeom,
					val
				FROM (
					SELECT
						public.ST_Intersection((gv).geom, geomin) AS intgeom,
						(gv).val
					FROM public.ST_DumpAsPolygons(rast, band) gv
					WHERE public.ST_Intersects((gv).geom, geomin)
				) foo
				WHERE NOT public.ST_IsEmpty(intgeom);
		ELSE
			-- If the geometry does not intersect with the raster, return an empty
			-- geometry and a null value
			RETURN QUERY
				SELECT
					emptygeom,
					NULL::float8
				FROM public.ST_GeomCollFromText('GEOMETRYCOLLECTION EMPTY', ST_SRID($1)) emptygeom;
		END IF;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersection(geography, geography)
 RETURNS geography
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.geography(public.ST_Transform(public.ST_Intersection(public.ST_Transform(public.geometry($1), public._ST_BestSRID($1, $2)), public.ST_Transform(public.geometry($2), public._ST_BestSRID($1, $2))), 4326))$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersection(rast1 raster, rast2 raster, nodataval double precision)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.st_intersection($1, 1, $2, 1, 'BOTH', ARRAY[$3, $3]) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersection(rast1 raster, rast2 raster, returnband text, nodataval double precision)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.st_intersection($1, 1, $2, 1, $3, ARRAY[$4, $4]) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersection(geom1 geometry, geom2 geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$intersection$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersection(rast1 raster, band1 integer, rast2 raster, band2 integer, returnband text DEFAULT 'BOTH'::text, nodataval double precision[] DEFAULT NULL::double precision[])
 RETURNS raster
 LANGUAGE plpgsql
 STABLE
AS $function$
	DECLARE
		rtn public.raster;
		_returnband text;
		newnodata1 float8;
		newnodata2 float8;
	BEGIN
		IF ST_SRID(rast1) != ST_SRID(rast2) THEN
			RAISE EXCEPTION 'The two rasters do not have the same SRID';
		END IF;

		newnodata1 := coalesce(nodataval[1], ST_BandNodataValue(rast1, band1), ST_MinPossibleValue(public.ST_BandPixelType(rast1, band1)));
		newnodata2 := coalesce(nodataval[2], ST_BandNodataValue(rast2, band2), ST_MinPossibleValue(public.ST_BandPixelType(rast2, band2)));

		_returnband := upper(returnband);

		rtn := NULL;
		CASE
			WHEN _returnband = 'BAND1' THEN
				rtn := public.ST_MapAlgebraExpr(rast1, band1, rast2, band2, '[rast1.val]', public.ST_BandPixelType(rast1, band1), 'INTERSECTION', newnodata1::text, newnodata1::text, newnodata1);
				rtn := public.ST_SetBandNodataValue(rtn, 1, newnodata1);
			WHEN _returnband = 'BAND2' THEN
				rtn := public.ST_MapAlgebraExpr(rast1, band1, rast2, band2, '[rast2.val]', public.ST_BandPixelType(rast2, band2), 'INTERSECTION', newnodata2::text, newnodata2::text, newnodata2);
				rtn := public.ST_SetBandNodataValue(rtn, 1, newnodata2);
			WHEN _returnband = 'BOTH' THEN
				rtn := public.ST_MapAlgebraExpr(rast1, band1, rast2, band2, '[rast1.val]', public.ST_BandPixelType(rast1, band1), 'INTERSECTION', newnodata1::text, newnodata1::text, newnodata1);
				rtn := ST_SetBandNodataValue(rtn, 1, newnodata1);
				rtn := ST_AddBand(rtn, ST_MapAlgebraExpr(rast1, band1, rast2, band2, '[rast2.val]', public.ST_BandPixelType(rast2, band2), 'INTERSECTION', newnodata2::text, newnodata2::text, newnodata2));
				rtn := ST_SetBandNodataValue(rtn, 2, newnodata2);
			ELSE
				RAISE EXCEPTION 'Unknown value provided for returnband: %', returnband;
				RETURN NULL;
		END CASE;

		RETURN rtn;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersection(text, text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_Intersection($1::public.geometry, $2::public.geometry);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersects(text, text)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_Intersects($1::public.geometry, $2::public.geometry);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersects(geography, geography)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.&&) $2 AND public._ST_Distance($1, $2, 0.0, false) < 0.00001$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersects(rast1 raster, rast2 raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT public.st_intersects($1, NULL::integer, $2, NULL::integer) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersects(rast1 raster, nband1 integer, rast2 raster, nband2 integer)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT $1 OPERATOR(public.&&) $3 AND CASE WHEN $2 IS NULL OR $4 IS NULL THEN public._st_intersects(public.st_convexhull($1), public.st_convexhull($3)) ELSE public._st_intersects($1, $2, $3, $4) END $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersects(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.&&) $2 AND public._ST_Intersects($1,$2)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersects(rast raster, nband integer, geom geometry)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT $1::public.geometry OPERATOR(public.&&) $3 AND public._st_intersects($3, $1, $2) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersects(rast raster, geom geometry, nband integer DEFAULT NULL::integer)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT $1::public.geometry OPERATOR(public.&&) $2 AND public._st_intersects($2, $1, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_intersects(geom geometry, rast raster, nband integer DEFAULT NULL::integer)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT $1 OPERATOR(public.&&) $2::public.geometry AND public._st_intersects($1, $2, $3); $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_invdistweight4ma(value double precision[], pos integer[], VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		_value double precision[][][];
		ndims int;

		k double precision DEFAULT 1.;
		_k double precision DEFAULT 1.;
		z double precision[];
		d double precision[];
		_d double precision;
		z0 double precision;

		_z integer;
		x integer;
		y integer;

		cx integer;
		cy integer;
		cv double precision;
		cw double precision DEFAULT NULL;

		w integer;
		h integer;
		max_dx double precision;
		max_dy double precision;
	BEGIN
--		RAISE NOTICE 'value = %', value;
--		RAISE NOTICE 'userargs = %', userargs;

		ndims := array_ndims(value);
		-- add a third dimension if 2-dimension
		IF ndims = 2 THEN
			_value := public._ST_convertarray4ma(value);
		ELSEIF ndims != 3 THEN
			RAISE EXCEPTION 'First parameter of function must be a 3-dimension array';
		ELSE
			_value := value;
		END IF;

		-- only use the first raster passed to this function
		IF array_length(_value, 1) > 1 THEN
			RAISE NOTICE 'Only using the values from the first raster';
		END IF;
		_z := array_lower(_value, 1);

		-- width and height (0-based)
		h := array_upper(_value, 2) - array_lower(_value, 2);
		w := array_upper(_value, 3) - array_lower(_value, 3);

		-- max distance from center pixel
		max_dx := w / 2;
		max_dy := h / 2;
--		RAISE NOTICE 'max_dx, max_dy = %, %', max_dx, max_dy;

		-- correct width and height (1-based)
		w := w + 1;
		h := h + 1;
--		RAISE NOTICE 'w, h = %, %', w, h;

		-- width and height should be odd numbers
		IF w % 2. != 1 THEN
			RAISE EXCEPTION 'Width of neighborhood array does not permit for a center pixel';
		END IF;
		IF h % 2. != 1 THEN
			RAISE EXCEPTION 'Height of neighborhood array does not permit for a center pixel';
		END IF;

		-- center pixel's coordinates
		cy := max_dy + array_lower(_value, 2);
		cx := max_dx + array_lower(_value, 3);
--		RAISE NOTICE 'cx, cy = %, %', cx, cy;

		-- if userargs provided, only use the first two args
		IF userargs IS NOT NULL AND array_ndims(userargs) = 1 THEN
			-- first arg is power factor
			k := userargs[array_lower(userargs, 1)]::double precision;
			IF k IS NULL THEN
				k := _k;
			ELSEIF k < 0. THEN
				RAISE NOTICE 'Power factor (< 0) must be between 0 and 1.  Defaulting to 0';
				k := 0.;
			ELSEIF k > 1. THEN
				RAISE NOTICE 'Power factor (> 1) must be between 0 and 1.  Defaulting to 1';
				k := 1.;
			END IF;

			-- second arg is what to do if center pixel has a value
			-- this will be a weight to apply for the center pixel
			IF array_length(userargs, 1) > 1 THEN
				cw := abs(userargs[array_lower(userargs, 1) + 1]::double precision);
				IF cw IS NOT NULL THEN
					IF cw < 0. THEN
						RAISE NOTICE 'Weight (< 0) of center pixel value must be between 0 and 1.  Defaulting to 0';
						cw := 0.;
					ELSEIF cw > 1 THEN
						RAISE NOTICE 'Weight (> 1) of center pixel value must be between 0 and 1.  Defaulting to 1';
						cw := 1.;
					END IF;
				END IF;
			END IF;
		END IF;
--		RAISE NOTICE 'k = %', k;
		k = abs(k) * -1;

		-- center pixel value
		cv := _value[_z][cy][cx];

		-- check to see if center pixel has value
--		RAISE NOTICE 'cw = %', cw;
		IF cw IS NULL AND cv IS NOT NULL THEN
			RETURN cv;
		END IF;

		FOR y IN array_lower(_value, 2)..array_upper(_value, 2) LOOP
			FOR x IN array_lower(_value, 3)..array_upper(_value, 3) LOOP
--				RAISE NOTICE 'value[%][%][%] = %', _z, y, x, _value[_z][y][x];

				-- skip NODATA values and center pixel
				IF _value[_z][y][x] IS NULL OR (x = cx AND y = cy) THEN
					CONTINUE;
				END IF;

				z := z || _value[_z][y][x];

				-- use pythagorean theorem
				_d := sqrt(power(cx - x, 2) + power(cy - y, 2));
--				RAISE NOTICE 'distance = %', _d;

				d := d || _d;
			END LOOP;
		END LOOP;
--		RAISE NOTICE 'z = %', z;
--		RAISE NOTICE 'd = %', d;

		-- neighborhood is NODATA
		IF z IS NULL OR array_length(z, 1) < 1 THEN
			-- center pixel has value
			IF cv IS NOT NULL THEN
				RETURN cv;
			ELSE
				RETURN NULL;
			END IF;
		END IF;

		z0 := 0;
		_d := 0;
		FOR x IN array_lower(z, 1)..array_upper(z, 1) LOOP
			d[x] := power(d[x], k);
			z[x] := z[x] * d[x];
			_d := _d + d[x];
			z0 := z0 + z[x];
		END LOOP;
		z0 := z0 / _d;
--		RAISE NOTICE 'z0 = %', z0;

		-- apply weight for center pixel if center pixel has value
		IF cv IS NOT NULL THEN
			z0 := (cw * cv) + ((1 - cw) * z0);
--			RAISE NOTICE '*z0 = %', z0;
		END IF;

		RETURN z0;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_isclosed(geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$LWGEOM_isclosed$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_iscollection(geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 5
AS '$libdir/postgis-2.5', $function$ST_IsCollection$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_iscoveragetile(rast raster, coverage raster, tilewidth integer, tileheight integer)
 RETURNS boolean
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
	DECLARE
		_rastmeta record;
		_covmeta record;
		cr record;
		max integer[];
		tile integer[];
		edge integer[];
	BEGIN
		IF NOT public.ST_SameAlignment(rast, coverage) THEN
			RAISE NOTICE 'Raster and coverage are not aligned';
			RETURN FALSE;
		END IF;

		_rastmeta := public.ST_Metadata(rast);
		_covmeta := public.ST_Metadata(coverage);

		-- get coverage grid coordinates of upper-left of rast
		cr := public.ST_WorldToRasterCoord(coverage, _rastmeta.upperleftx, _rastmeta.upperlefty);

		-- rast is not part of coverage
		IF
			(cr.columnx < 1 OR cr.columnx > _covmeta.width) OR
			(cr.rowy < 1 OR cr.rowy > _covmeta.height)
		THEN
			RAISE NOTICE 'Raster is not in the coverage';
			RETURN FALSE;
		END IF;

		-- rast isn't on the coverage's grid
		IF
			((cr.columnx - 1) % tilewidth != 0) OR
			((cr.rowy - 1) % tileheight != 0)
		THEN
			RAISE NOTICE 'Raster is not aligned to tile grid of coverage';
			RETURN FALSE;
		END IF;

		-- max # of tiles on X and Y for coverage
		max[0] := ceil(_covmeta.width::double precision / tilewidth::double precision)::integer;
		max[1] := ceil(_covmeta.height::double precision / tileheight::double precision)::integer;

		-- tile # of rast in coverge
		tile[0] := (cr.columnx / tilewidth) + 1;
		tile[1] := (cr.rowy / tileheight) + 1;

		-- inner tile
		IF tile[0] < max[0] AND tile[1] < max[1] THEN
			IF
				(_rastmeta.width != tilewidth) OR
				(_rastmeta.height != tileheight)
			THEN
				RAISE NOTICE 'Raster width/height is invalid for interior tile of coverage';
				RETURN FALSE;
			ELSE
				RETURN TRUE;
			END IF;
		END IF;

		-- edge tile

		-- edge tile may have same size as inner tile
		IF
			(_rastmeta.width = tilewidth) AND
			(_rastmeta.height = tileheight)
		THEN
			RETURN TRUE;
		END IF;

		-- get edge tile width and height
		edge[0] := _covmeta.width - ((max[0] - 1) * tilewidth);
		edge[1] := _covmeta.height - ((max[1] - 1) * tileheight);

		-- edge tile not of expected tile size
		-- right and bottom
		IF tile[0] = max[0] AND tile[1] = max[1] THEN
			IF
				_rastmeta.width != edge[0] OR
				_rastmeta.height != edge[1]
			THEN
				RAISE NOTICE 'Raster width/height is invalid for right-most AND bottom-most tile of coverage';
				RETURN FALSE;
			END IF;
		ELSEIF tile[0] = max[0] THEN
			IF
				_rastmeta.width != edge[0] OR
				_rastmeta.height != tileheight
			THEN
				RAISE NOTICE 'Raster width/height is invalid for right-most tile of coverage';
				RETURN FALSE;
			END IF;
		ELSE
			IF
				_rastmeta.width != tilewidth OR
				_rastmeta.height != edge[1]
			THEN
				RAISE NOTICE 'Raster width/height is invalid for bottom-most tile of coverage';
				RETURN FALSE;
			END IF;
		END IF;

		RETURN TRUE;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_isempty(geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$LWGEOM_isempty$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_isempty(rast raster)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_isEmpty$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_ispolygonccw(geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$ST_IsPolygonCCW$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_ispolygoncw(geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$ST_IsPolygonCW$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_isring(geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$isring$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_issimple(geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 25
AS '$libdir/postgis-2.5', $function$issimple$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_isvalid(geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 1000
AS '$libdir/postgis-2.5', $function$isvalid$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_isvalid(geometry, integer)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT (public.ST_isValidDetail($1, $2)).valid$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_isvaliddetail(geometry)
 RETURNS valid_detail
 LANGUAGE c
 IMMUTABLE STRICT COST 1000
AS '$libdir/postgis-2.5', $function$isvaliddetail$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_isvaliddetail(geometry, integer)
 RETURNS valid_detail
 LANGUAGE c
 IMMUTABLE STRICT COST 1000
AS '$libdir/postgis-2.5', $function$isvaliddetail$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_isvalidreason(geometry)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT COST 1000
AS '$libdir/postgis-2.5', $function$isvalidreason$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_isvalidreason(geometry, integer)
 RETURNS text
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
SELECT CASE WHEN valid THEN 'Valid Geometry' ELSE reason END FROM (
	SELECT (public.ST_isValidDetail($1, $2)).*
) foo
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_isvalidtrajectory(geometry)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_IsValidTrajectory$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_length(text)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_Length($1::public.geometry);  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_length(geog geography, use_spheroid boolean DEFAULT true)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$geography_length$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_length(geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$LWGEOM_length2d_linestring$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_length2d(geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$LWGEOM_length2d_linestring$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_length2d_spheroid(geometry, spheroid)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Length2D_Spheroid', 'ST_Length2DSpheroid', '2.2.0');
    SELECT public.ST_Length2DSpheroid($1,$2);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_length2dspheroid(geometry, spheroid)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 500
AS '$libdir/postgis-2.5', $function$LWGEOM_length2d_ellipsoid$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_length_spheroid(geometry, spheroid)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Length_Spheroid', 'ST_LengthSpheroid', '2.2.0');
    SELECT public.ST_LengthSpheroid($1,$2);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_lengthspheroid(geometry, spheroid)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 500
AS '$libdir/postgis-2.5', $function$LWGEOM_length_ellipsoid_linestring$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_line_interpolate_point(geometry, double precision)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Line_Interpolate_Point', 'ST_LineInterpolatePoint', '2.1.0');
    SELECT public.ST_LineInterpolatePoint($1, $2);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_line_locate_point(geom1 geometry, geom2 geometry)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Line_Locate_Point', 'ST_LineLocatePoint', '2.1.0');
     SELECT public.ST_LineLocatePoint($1, $2);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_line_substring(geometry, double precision, double precision)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Line_Substring', 'ST_LineSubstring', '2.1.0');
     SELECT public.ST_LineSubstring($1, $2, $3);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_linecrossingdirection(geom1 geometry, geom2 geometry)
 RETURNS integer
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT CASE WHEN NOT $1 OPERATOR(public.&&) $2 THEN 0 ELSE public._ST_LineCrossingDirection($1,$2) END $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_linefromencodedpolyline(text, integer DEFAULT 5)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$line_from_encoded_polyline$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_linefrommultipoint(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_line_from_mpoint$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_linefromtext(text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromText($1)) = 'LINESTRING'
	THEN public.ST_GeomFromText($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_linefromtext(text, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromText($1, $2)) = 'LINESTRING'
	THEN public.ST_GeomFromText($1,$2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_linefromwkb(bytea, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1, $2)) = 'LINESTRING'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_linefromwkb(bytea)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1)) = 'LINESTRING'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_lineinterpolatepoint(geometry, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_line_interpolate_point$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_lineinterpolatepoints(geometry, double precision, repeat boolean DEFAULT true)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_line_interpolate_point$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_linelocatepoint(geom1 geometry, geom2 geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_line_locate_point$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_linemerge(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$linemerge$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_linestringfromwkb(bytea)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1)) = 'LINESTRING'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_linestringfromwkb(bytea, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1, $2)) = 'LINESTRING'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_linesubstring(geometry, double precision, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_line_substring$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_linetocurve(geometry geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_line_desegmentize$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_locate_along_measure(geometry, double precision)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_locate_between_measures($1, $2, $2) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_locate_between_measures(geometry, double precision, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_locate_between_m$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_locatealong(geometry geometry, measure double precision, leftrightoffset double precision DEFAULT 0.0)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_LocateAlong$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_locatebetween(geometry geometry, frommeasure double precision, tomeasure double precision, leftrightoffset double precision DEFAULT 0.0)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_LocateBetween$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_locatebetweenelevations(geometry geometry, fromelevation double precision, toelevation double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_LocateBetweenElevations$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_longestline(geom1 geometry, geom2 geometry)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public._ST_LongestLine(public.ST_ConvexHull($1), public.ST_ConvexHull($2))$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_m(geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_m_point$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_makebox2d(geom1 geometry, geom2 geometry)
 RETURNS box2d
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX2D_construct$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_makeemptycoverage(tilewidth integer, tileheight integer, width integer, height integer, upperleftx double precision, upperlefty double precision, scalex double precision, scaley double precision, skewx double precision, skewy double precision, srid integer DEFAULT 0)
 RETURNS SETOF raster
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
    DECLARE
        ulx double precision;  -- upper left x of raster
        uly double precision;  -- upper left y of raster
        rw int;                -- raster width (may change at edges)
        rh int;                -- raster height (may change at edges)
        x int;                 -- x index of coverage
        y int;                 -- y index of coverage
        template public.raster;       -- an empty template raster, where each cell
                               -- represents a tile in the coverage
        minY double precision;
        maxX double precision;
    BEGIN
        template := public.ST_MakeEmptyRaster(
            ceil(width::float8/tilewidth)::int,
            ceil(height::float8/tileheight)::int,
            upperleftx,
            upperlefty,
            tilewidth * scalex,
            tileheight * scaley,
            tileheight * skewx,
            tilewidth * skewy,
            srid
        );

        FOR y IN 1..st_height(template) LOOP
            maxX := public.ST_RasterToWorldCoordX(template, 1, y) + width * scalex;
            FOR x IN 1..st_width(template) LOOP
                minY := public.ST_RasterToWorldCoordY(template, x, 1) + height * scaley;
                uly := public.ST_RasterToWorldCoordY(template, x, y);
                IF uly + (tileheight * scaley) < minY THEN
                    --raise notice 'uly, minY: %, %', uly, minY;
                    rh := ceil((minY - uly)/scaleY)::int;
                ELSE
                    rh := tileheight;
                END IF;

                ulx := public.ST_RasterToWorldCoordX(template, x, y);
                IF ulx + (tilewidth * scalex) > maxX THEN
                    --raise notice 'ulx, maxX: %, %', ulx, maxX;
                    rw := ceil((maxX - ulx)/scaleX)::int;
                ELSE
                    rw := tilewidth;
                END IF;

                RETURN NEXT public.ST_MakeEmptyRaster(rw, rh, ulx, uly, scalex, scaley, skewx, skewy, srid);
            END LOOP;
        END LOOP;
    END;
    $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_makeemptyraster(width integer, height integer, upperleftx double precision, upperlefty double precision, pixelsize double precision)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT  public.ST_makeemptyraster($1, $2, $3, $4, $5, -($5), 0, 0, public.ST_SRID('POINT(0 0)'::public.geometry)) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_makeemptyraster(width integer, height integer, upperleftx double precision, upperlefty double precision, scalex double precision, scaley double precision, skewx double precision, skewy double precision, srid integer DEFAULT 0)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_makeEmpty$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_makeemptyraster(rast raster)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
		DECLARE
			w int;
			h int;
			ul_x double precision;
			ul_y double precision;
			scale_x double precision;
			scale_y double precision;
			skew_x double precision;
			skew_y double precision;
			sr_id int;
		BEGIN
			SELECT width, height, upperleftx, upperlefty, scalex, scaley, skewx, skewy, srid INTO w, h, ul_x, ul_y, scale_x, scale_y, skew_x, skew_y, sr_id FROM public.ST_Metadata(rast);
			RETURN  public.ST_makeemptyraster(w, h, ul_x, ul_y, scale_x, scale_y, skew_x, skew_y, sr_id);
		END;
    $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_makeenvelope(double precision, double precision, double precision, double precision, integer DEFAULT 0)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_MakeEnvelope$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_makeline(geom1 geometry, geom2 geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_makeline$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_makeline(geometry[])
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_makeline_garray$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_makepoint(double precision, double precision, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_makepoint$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_makepoint(double precision, double precision, double precision, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_makepoint$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_makepoint(double precision, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_makepoint$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_makepointm(double precision, double precision, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_makepoint3dm$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_makepolygon(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_makepoly$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_makepolygon(geometry, geometry[])
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_makepoly$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_makevalid(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_MakeValid$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebra(rast1 raster, nband1 integer, rast2 raster, nband2 integer, callbackfunc regprocedure, pixeltype text DEFAULT NULL::text, extenttype text DEFAULT 'INTERSECTION'::text, customextent raster DEFAULT NULL::raster, distancex integer DEFAULT 0, distancey integer DEFAULT 0, VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_MapAlgebra(ARRAY[ROW($1, $2), ROW($3, $4)]::rastbandarg[], $5, $6, $9, $10, $7, $8,NULL::double precision [],NULL::boolean, VARIADIC $11) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebra(rast raster, pixeltype text, expression text, nodataval double precision DEFAULT NULL::double precision)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_mapalgebra($1, 1, $2, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebra(rast raster, nband integer, callbackfunc regprocedure, mask double precision[], weighted boolean, pixeltype text DEFAULT NULL::text, extenttype text DEFAULT 'INTERSECTION'::text, customextent raster DEFAULT NULL::raster, VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$
	select public._ST_mapalgebra(ARRAY[ROW($1,$2)]::rastbandarg[],$3,$6,NULL::integer,NULL::integer,$7,$8,$4,$5,VARIADIC $9)
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebra(rast raster, nband integer, pixeltype text, expression text, nodataval double precision DEFAULT NULL::double precision)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_mapalgebra(ARRAY[ROW($1, $2)]::rastbandarg[], $4, $3, 'FIRST', $5::text) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebra(rastbandargset rastbandarg[], callbackfunc regprocedure, pixeltype text DEFAULT NULL::text, extenttype text DEFAULT 'INTERSECTION'::text, customextent raster DEFAULT NULL::raster, distancex integer DEFAULT 0, distancey integer DEFAULT 0, VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_MapAlgebra($1, $2, $3, $6, $7, $4, $5,NULL::double precision [],NULL::boolean, VARIADIC $8) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebra(rast1 raster, band1 integer, rast2 raster, band2 integer, expression text, pixeltype text DEFAULT NULL::text, extenttype text DEFAULT 'INTERSECTION'::text, nodata1expr text DEFAULT NULL::text, nodata2expr text DEFAULT NULL::text, nodatanodataval double precision DEFAULT NULL::double precision)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_mapalgebra(ARRAY[ROW($1, $2), ROW($3, $4)]::rastbandarg[], $5, $6, $7, $8, $9, $10) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebra(rast raster, nband integer[], callbackfunc regprocedure, pixeltype text DEFAULT NULL::text, extenttype text DEFAULT 'FIRST'::text, customextent raster DEFAULT NULL::raster, distancex integer DEFAULT 0, distancey integer DEFAULT 0, VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		x int;
		argset rastbandarg[];
	BEGIN
		IF $2 IS NULL OR array_ndims($2) < 1 OR array_length($2, 1) < 1 THEN
			RAISE EXCEPTION 'Populated 1D array must be provided for nband';
			RETURN NULL;
		END IF;

		FOR x IN array_lower($2, 1)..array_upper($2, 1) LOOP
			IF $2[x] IS NULL THEN
				CONTINUE;
			END IF;

			argset := argset || ROW($1, $2[x])::rastbandarg;
		END LOOP;

		IF array_length(argset, 1) < 1 THEN
			RAISE EXCEPTION 'Populated 1D array must be provided for nband';
			RETURN NULL;
		END IF;

		RETURN public._ST_MapAlgebra(argset, $3, $4, $7, $8, $5, $6,NULL::double precision [],NULL::boolean, VARIADIC $9);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebra(rast raster, nband integer, callbackfunc regprocedure, pixeltype text DEFAULT NULL::text, extenttype text DEFAULT 'FIRST'::text, customextent raster DEFAULT NULL::raster, distancex integer DEFAULT 0, distancey integer DEFAULT 0, VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_MapAlgebra(ARRAY[ROW($1, $2)]::rastbandarg[], $3, $4, $7, $8, $5, $6,NULL::double precision [],NULL::boolean, VARIADIC $9) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebra(rast1 raster, rast2 raster, expression text, pixeltype text DEFAULT NULL::text, extenttype text DEFAULT 'INTERSECTION'::text, nodata1expr text DEFAULT NULL::text, nodata2expr text DEFAULT NULL::text, nodatanodataval double precision DEFAULT NULL::double precision)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_mapalgebra($1, 1, $2, 1, $3, $4, $5, $6, $7, $8) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebraexpr(rast raster, pixeltype text, expression text, nodataval double precision DEFAULT NULL::double precision)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_mapalgebraexpr($1, 1, $2, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebraexpr(rast raster, band integer, pixeltype text, expression text, nodataval double precision DEFAULT NULL::double precision)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_mapAlgebraExpr$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebraexpr(rast1 raster, rast2 raster, expression text, pixeltype text DEFAULT NULL::text, extenttype text DEFAULT 'INTERSECTION'::text, nodata1expr text DEFAULT NULL::text, nodata2expr text DEFAULT NULL::text, nodatanodataval double precision DEFAULT NULL::double precision)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_mapalgebraexpr($1, 1, $2, 1, $3, $4, $5, $6, $7, $8) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebraexpr(rast1 raster, band1 integer, rast2 raster, band2 integer, expression text, pixeltype text DEFAULT NULL::text, extenttype text DEFAULT 'INTERSECTION'::text, nodata1expr text DEFAULT NULL::text, nodata2expr text DEFAULT NULL::text, nodatanodataval double precision DEFAULT NULL::double precision)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_mapAlgebra2$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebrafct(rast raster, onerastuserfunc regprocedure, VARIADIC args text[])
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_mapalgebrafct($1, 1, NULL, $2, VARIADIC $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebrafct(rast raster, onerastuserfunc regprocedure)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_mapalgebrafct($1, 1, NULL, $2, NULL) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebrafct(rast1 raster, band1 integer, rast2 raster, band2 integer, tworastuserfunc regprocedure, pixeltype text DEFAULT NULL::text, extenttype text DEFAULT 'INTERSECTION'::text, VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_mapAlgebra2$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebrafct(rast raster, pixeltype text, onerastuserfunc regprocedure, VARIADIC args text[])
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_mapalgebrafct($1, 1, $2, $3, VARIADIC $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebrafct(rast raster, band integer, onerastuserfunc regprocedure)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_mapalgebrafct($1, $2, NULL, $3, NULL) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebrafct(rast raster, band integer, onerastuserfunc regprocedure, VARIADIC args text[])
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_mapalgebrafct($1, $2, NULL, $3, VARIADIC $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebrafct(rast raster, band integer, pixeltype text, onerastuserfunc regprocedure)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_mapalgebrafct($1, $2, $3, $4, NULL) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebrafct(rast raster, band integer, pixeltype text, onerastuserfunc regprocedure, VARIADIC args text[])
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_mapAlgebraFct$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebrafct(rast1 raster, rast2 raster, tworastuserfunc regprocedure, pixeltype text DEFAULT NULL::text, extenttype text DEFAULT 'INTERSECTION'::text, VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_mapalgebrafct($1, 1, $2, 1, $3, $4, $5, VARIADIC $6) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebrafct(rast raster, pixeltype text, onerastuserfunc regprocedure)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_mapalgebrafct($1, 1, $2, $3, NULL) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mapalgebrafctngb(rast raster, band integer, pixeltype text, ngbwidth integer, ngbheight integer, onerastngbuserfunc regprocedure, nodatamode text, VARIADIC args text[])
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_mapAlgebraFctNgb$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_max4ma(value double precision[], pos integer[], VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		_value double precision[][][];
		max double precision;
		x int;
		y int;
		z int;
		ndims int;
	BEGIN
		max := '-Infinity'::double precision;

		ndims := array_ndims(value);
		-- add a third dimension if 2-dimension
		IF ndims = 2 THEN
			_value := public._ST_convertarray4ma(value);
		ELSEIF ndims != 3 THEN
			RAISE EXCEPTION 'First parameter of function must be a 3-dimension array';
		ELSE
			_value := value;
		END IF;

		-- raster
		FOR z IN array_lower(_value, 1)..array_upper(_value, 1) LOOP
			-- row
			FOR y IN array_lower(_value, 2)..array_upper(_value, 2) LOOP
				-- column
				FOR x IN array_lower(_value, 3)..array_upper(_value, 3) LOOP
					IF _value[z][y][x] IS NULL THEN
						IF array_length(userargs, 1) > 0 THEN
							_value[z][y][x] = userargs[array_lower(userargs, 1)]::double precision;
						ELSE
							CONTINUE;
						END IF;
					END IF;

					IF _value[z][y][x] > max THEN
						max := _value[z][y][x];
					END IF;
				END LOOP;
			END LOOP;
		END LOOP;

		IF max = '-Infinity'::double precision THEN
			RETURN NULL;
		END IF;

		RETURN max;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_max4ma(matrix double precision[], nodatamode text, VARIADIC args text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
    DECLARE
        _matrix float[][];
        max float;
    BEGIN
        _matrix := matrix;
        max := '-Infinity'::float;
        FOR x in array_lower(_matrix, 1)..array_upper(_matrix, 1) LOOP
            FOR y in array_lower(_matrix, 2)..array_upper(_matrix, 2) LOOP
                IF _matrix[x][y] IS NULL THEN
                    IF NOT nodatamode = 'ignore' THEN
                        _matrix[x][y] := nodatamode::float;
                    END IF;
                END IF;
                IF max < _matrix[x][y] THEN
                    max := _matrix[x][y];
                END IF;
            END LOOP;
        END LOOP;
        RETURN max;
    END;
    $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_maxdistance(geom1 geometry, geom2 geometry)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public._ST_MaxDistance(public.ST_ConvexHull($1), public.ST_ConvexHull($2))$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mean4ma(value double precision[], pos integer[], VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		_value double precision[][][];
		sum double precision;
		count int;
		x int;
		y int;
		z int;
		ndims int;
	BEGIN
		sum := 0;
		count := 0;

		ndims := array_ndims(value);
		-- add a third dimension if 2-dimension
		IF ndims = 2 THEN
			_value := public._ST_convertarray4ma(value);
		ELSEIF ndims != 3 THEN
			RAISE EXCEPTION 'First parameter of function must be a 3-dimension array';
		ELSE
			_value := value;
		END IF;

		-- raster
		FOR z IN array_lower(_value, 1)..array_upper(_value, 1) LOOP
			-- row
			FOR y IN array_lower(_value, 2)..array_upper(_value, 2) LOOP
				-- column
				FOR x IN array_lower(_value, 3)..array_upper(_value, 3) LOOP
					IF _value[z][y][x] IS NULL THEN
						IF array_length(userargs, 1) > 0 THEN
							_value[z][y][x] = userargs[array_lower(userargs, 1)]::double precision;
						ELSE
							CONTINUE;
						END IF;
					END IF;

					sum := sum + _value[z][y][x];
					count := count + 1;
				END LOOP;
			END LOOP;
		END LOOP;

		IF count < 1 THEN
			RETURN NULL;
		END IF;

		RETURN sum / count::double precision;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mean4ma(matrix double precision[], nodatamode text, VARIADIC args text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
    DECLARE
        _matrix float[][];
        sum float;
        count float;
    BEGIN
        _matrix := matrix;
        sum := 0;
        count := 0;
        FOR x in array_lower(matrix, 1)..array_upper(matrix, 1) LOOP
            FOR y in array_lower(matrix, 2)..array_upper(matrix, 2) LOOP
                IF _matrix[x][y] IS NULL THEN
                    IF nodatamode = 'ignore' THEN
                        _matrix[x][y] := 0;
                    ELSE
                        _matrix[x][y] := nodatamode::float;
                        count := count + 1;
                    END IF;
                ELSE
                    count := count + 1;
                END IF;
                sum := sum + _matrix[x][y];
            END LOOP;
        END LOOP;
        IF count = 0 THEN
            RETURN NULL;
        END IF;
        RETURN sum / count;
    END;
    $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mem_size(geometry)
 RETURNS integer
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Mem_Size', 'ST_MemSize', '2.2.0');
    SELECT public.ST_MemSize($1);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_memsize(raster)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_memsize$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_memsize(geometry)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT COST 5
AS '$libdir/postgis-2.5', $function$LWGEOM_mem_size$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_metadata(rast raster, OUT upperleftx double precision, OUT upperlefty double precision, OUT width integer, OUT height integer, OUT scalex double precision, OUT scaley double precision, OUT skewx double precision, OUT skewy double precision, OUT srid integer, OUT numbands integer)
 RETURNS record
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_metadata$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_min4ma(matrix double precision[], nodatamode text, VARIADIC args text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
    DECLARE
        _matrix float[][];
        min float;
    BEGIN
        _matrix := matrix;
        min := 'Infinity'::float;
        FOR x in array_lower(_matrix, 1)..array_upper(_matrix, 1) LOOP
            FOR y in array_lower(_matrix, 2)..array_upper(_matrix, 2) LOOP
                IF _matrix[x][y] IS NULL THEN
                    IF NOT nodatamode = 'ignore' THEN
                        _matrix[x][y] := nodatamode::float;
                    END IF;
                END IF;
                IF min > _matrix[x][y] THEN
                    min := _matrix[x][y];
                END IF;
            END LOOP;
        END LOOP;
        RETURN min;
    END;
    $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_min4ma(value double precision[], pos integer[], VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		_value double precision[][][];
		min double precision;
		x int;
		y int;
		z int;
		ndims int;
	BEGIN
		min := 'Infinity'::double precision;

		ndims := array_ndims(value);
		-- add a third dimension if 2-dimension
		IF ndims = 2 THEN
			_value := public._ST_convertarray4ma(value);
		ELSEIF ndims != 3 THEN
			RAISE EXCEPTION 'First parameter of function must be a 3-dimension array';
		ELSE
			_value := value;
		END IF;

		-- raster
		FOR z IN array_lower(_value, 1)..array_upper(_value, 1) LOOP
			-- row
			FOR y IN array_lower(_value, 2)..array_upper(_value, 2) LOOP
				-- column
				FOR x IN array_lower(_value, 3)..array_upper(_value, 3) LOOP
					IF _value[z][y][x] IS NULL THEN
						IF array_length(userargs, 1) > 0 THEN
							_value[z][y][x] = userargs[array_lower(userargs, 1)]::double precision;
						ELSE
							CONTINUE;
						END IF;
					END IF;

					IF _value[z][y][x] < min THEN
						min := _value[z][y][x];
					END IF;
				END LOOP;
			END LOOP;
		END LOOP;

		IF min = 'Infinity'::double precision THEN
			RETURN NULL;
		END IF;

		RETURN min;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_minconvexhull(rast raster, nband integer DEFAULT NULL::integer)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_convex_hull$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mindist4ma(value double precision[], pos integer[], VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		_value double precision[][][];
		ndims int;

		d double precision DEFAULT NULL;
		_d double precision;

		z integer;
		x integer;
		y integer;

		cx integer;
		cy integer;
		cv double precision;

		w integer;
		h integer;
		max_dx double precision;
		max_dy double precision;
	BEGIN

		ndims := array_ndims(value);
		-- add a third dimension if 2-dimension
		IF ndims = 2 THEN
			_value := public._ST_convertarray4ma(value);
		ELSEIF ndims != 3 THEN
			RAISE EXCEPTION 'First parameter of function must be a 3-dimension array';
		ELSE
			_value := value;
		END IF;

		-- only use the first raster passed to this function
		IF array_length(_value, 1) > 1 THEN
			RAISE NOTICE 'Only using the values from the first raster';
		END IF;
		z := array_lower(_value, 1);

		-- width and height (0-based)
		h := array_upper(_value, 2) - array_lower(_value, 2);
		w := array_upper(_value, 3) - array_lower(_value, 3);

		-- max distance from center pixel
		max_dx := w / 2;
		max_dy := h / 2;

		-- correct width and height (1-based)
		w := w + 1;
		h := h + 1;

		-- width and height should be odd numbers
		IF w % 2. != 1 THEN
			RAISE EXCEPTION 'Width of neighborhood array does not permit for a center pixel';
		END IF;
		IF h % 2. != 1 THEN
			RAISE EXCEPTION 'Height of neighborhood array does not permit for a center pixel';
		END IF;

		-- center pixel's coordinates
		cy := max_dy + array_lower(_value, 2);
		cx := max_dx + array_lower(_value, 3);

		-- center pixel value
		cv := _value[z][cy][cx];

		-- check to see if center pixel has value
		IF cv IS NOT NULL THEN
			RETURN 0.;
		END IF;

		FOR y IN array_lower(_value, 2)..array_upper(_value, 2) LOOP
			FOR x IN array_lower(_value, 3)..array_upper(_value, 3) LOOP

				-- skip NODATA values and center pixel
				IF _value[z][y][x] IS NULL OR (x = cx AND y = cy) THEN
					CONTINUE;
				END IF;

				-- use pythagorean theorem
				_d := sqrt(power(cx - x, 2) + power(cy - y, 2));
--				RAISE NOTICE 'distance = %', _d;

				IF d IS NULL OR _d < d THEN
					d := _d;
				END IF;
			END LOOP;
		END LOOP;
--		RAISE NOTICE 'd = %', d;

		RETURN d;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_minimumboundingcircle(inputgeom geometry, segs_per_quarter integer DEFAULT 48)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_MinimumBoundingCircle$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_minimumboundingradius(geometry, OUT center geometry, OUT radius double precision)
 RETURNS record
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_MinimumBoundingRadius$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_minimumclearance(geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_MinimumClearance$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_minimumclearanceline(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_MinimumClearanceLine$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_minpossiblevalue(pixeltype text)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_minPossibleValue$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mlinefromtext(text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromText($1)) = 'MULTILINESTRING'
	THEN public.ST_GeomFromText($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mlinefromtext(text, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE
	WHEN public.geometrytype(public.ST_GeomFromText($1, $2)) = 'MULTILINESTRING'
	THEN public.ST_GeomFromText($1,$2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mlinefromwkb(bytea)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1)) = 'MULTILINESTRING'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mlinefromwkb(bytea, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1, $2)) = 'MULTILINESTRING'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mpointfromtext(text, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromText($1, $2)) = 'MULTIPOINT'
	THEN ST_GeomFromText($1, $2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mpointfromtext(text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromText($1)) = 'MULTIPOINT'
	THEN public.ST_GeomFromText($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mpointfromwkb(bytea)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1)) = 'MULTIPOINT'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mpointfromwkb(bytea, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1, $2)) = 'MULTIPOINT'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mpolyfromtext(text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromText($1)) = 'MULTIPOLYGON'
	THEN public.ST_GeomFromText($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mpolyfromtext(text, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromText($1, $2)) = 'MULTIPOLYGON'
	THEN public.ST_GeomFromText($1,$2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mpolyfromwkb(bytea, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1, $2)) = 'MULTIPOLYGON'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_mpolyfromwkb(bytea)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1)) = 'MULTIPOLYGON'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_multi(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_force_multi$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_multilinefromwkb(bytea)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1)) = 'MULTILINESTRING'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_multilinestringfromtext(text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_MLineFromText($1)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_multilinestringfromtext(text, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_MLineFromText($1, $2)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_multipointfromtext(text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_MPointFromText($1)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_multipointfromwkb(bytea)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1)) = 'MULTIPOINT'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_multipointfromwkb(bytea, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1,$2)) = 'MULTIPOINT'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_multipolyfromwkb(bytea, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1, $2)) = 'MULTIPOLYGON'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_multipolyfromwkb(bytea)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1)) = 'MULTIPOLYGON'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_multipolygonfromtext(text, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_MPolyFromText($1, $2)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_multipolygonfromtext(text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_MPolyFromText($1)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_ndims(geometry)
 RETURNS smallint
 LANGUAGE c
 IMMUTABLE STRICT COST 5
AS '$libdir/postgis-2.5', $function$LWGEOM_ndims$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_nearestvalue(rast raster, band integer, pt geometry, exclude_nodata_value boolean DEFAULT true)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_nearestValue$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_nearestvalue(rast raster, pt geometry, exclude_nodata_value boolean DEFAULT true)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT st_nearestvalue($1, 1, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_nearestvalue(rast raster, columnx integer, rowy integer, exclude_nodata_value boolean DEFAULT true)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.st_nearestvalue($1, 1, public.st_setsrid(public.st_makepoint(public.st_rastertoworldcoordx($1, $2, $3), public.st_rastertoworldcoordy($1, $2, $3)), public.st_srid($1)), $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_nearestvalue(rast raster, band integer, columnx integer, rowy integer, exclude_nodata_value boolean DEFAULT true)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.st_nearestvalue($1, $2, public.st_setsrid(public.st_makepoint(public.st_rastertoworldcoordx($1, $3, $4), public.st_rastertoworldcoordy($1, $3, $4)), public.st_srid($1)), $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_neighborhood(rast raster, band integer, columnx integer, rowy integer, distancex integer, distancey integer, exclude_nodata_value boolean DEFAULT true)
 RETURNS double precision[]
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_neighborhood($1, $2, $3, $4, $5, $6, $7) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_neighborhood(rast raster, band integer, pt geometry, distancex integer, distancey integer, exclude_nodata_value boolean DEFAULT true)
 RETURNS double precision[]
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
	DECLARE
		wx double precision;
		wy double precision;
		rtn double precision[][];
	BEGIN
		IF (public.st_geometrytype($3) != 'ST_Point') THEN
			RAISE EXCEPTION 'Attempting to get the neighbor of a pixel with a non-point geometry';
		END IF;

		IF public.ST_SRID(rast) != public.ST_SRID(pt) THEN
			RAISE EXCEPTION 'Raster and geometry do not have the same SRID';
		END IF;

		wx := st_x($3);
		wy := st_y($3);

		SELECT public._ST_neighborhood(
			$1, $2,
			public.st_worldtorastercoordx(rast, wx, wy),
			public.st_worldtorastercoordy(rast, wx, wy),
			$4, $5,
			$6
		) INTO rtn;
		RETURN rtn;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_neighborhood(rast raster, columnx integer, rowy integer, distancex integer, distancey integer, exclude_nodata_value boolean DEFAULT true)
 RETURNS double precision[]
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_neighborhood($1, 1, $2, $3, $4, $5, $6) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_neighborhood(rast raster, pt geometry, distancex integer, distancey integer, exclude_nodata_value boolean DEFAULT true)
 RETURNS double precision[]
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.st_neighborhood($1, 1, $2, $3, $4, $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_node(g geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_Node$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_normalize(geom geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_Normalize$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_notsamealignmentreason(rast1 raster, rast2 raster)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_notSameAlignmentReason$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_npoints(geometry)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$LWGEOM_npoints$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_nrings(geometry)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_nrings$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_numbands(raster)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getNumBands$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_numgeometries(geometry)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_numgeometries_collection$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_numinteriorring(geometry)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_numinteriorrings_polygon$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_numinteriorrings(geometry)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_numinteriorrings_polygon$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_numpatches(geometry)
 RETURNS integer
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.ST_GeometryType($1) = 'ST_PolyhedralSurface'
	THEN public.ST_NumGeometries($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_numpoints(geometry)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_numpoints_linestring$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_offsetcurve(line geometry, distance double precision, params text DEFAULT ''::text)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_OffsetCurve$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_orderingequals(geometrya geometry, geometryb geometry)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$
	SELECT $1 OPERATOR(public.~=) $2 AND public._ST_OrderingEquals($1, $2)
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_orientedenvelope(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_OrientedEnvelope$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_overlaps(rast1 raster, rast2 raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT public.st_overlaps($1, NULL::integer, $2, NULL::integer) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_overlaps(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.&&) $2 AND public._ST_Overlaps($1,$2)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_overlaps(rast1 raster, nband1 integer, rast2 raster, nband2 integer)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT $1 OPERATOR(public.&&) $3 AND CASE WHEN $2 IS NULL OR $4 IS NULL THEN public._st_overlaps(public.st_convexhull($1), public.st_convexhull($3)) ELSE public._ST_overlaps($1, $2, $3, $4) END $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_patchn(geometry, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.ST_GeometryType($1) = 'ST_PolyhedralSurface'
	THEN public.ST_GeometryN($1, $2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_perimeter(geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$LWGEOM_perimeter2d_poly$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_perimeter(geog geography, use_spheroid boolean DEFAULT true)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$geography_perimeter$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_perimeter2d(geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT COST 10
AS '$libdir/postgis-2.5', $function$LWGEOM_perimeter2d_poly$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pixelascentroid(rast raster, x integer, y integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_Centroid(geom) FROM public._ST_pixelaspolygons($1, NULL, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pixelascentroids(rast raster, band integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, OUT geom geometry, OUT val double precision, OUT x integer, OUT y integer)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_Centroid(geom), val, x, y FROM public._ST_pixelaspolygons($1, $2, NULL, NULL, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pixelaspoint(rast raster, x integer, y integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT ST_PointN(ST_ExteriorRing(geom), 1) FROM public._ST_pixelaspolygons($1, NULL, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pixelaspoints(rast raster, band integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, OUT geom geometry, OUT val double precision, OUT x integer, OUT y integer)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_PointN(  public.ST_ExteriorRing(geom), 1), val, x, y FROM public._ST_pixelaspolygons($1, $2, NULL, NULL, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pixelaspolygon(rast raster, x integer, y integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT geom FROM public._ST_pixelaspolygons($1, NULL, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pixelaspolygons(rast raster, band integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, OUT geom geometry, OUT val double precision, OUT x integer, OUT y integer)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT geom, val, x, y FROM public._ST_pixelaspolygons($1, $2, NULL, NULL, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pixelheight(raster)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getPixelHeight$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pixelofvalue(rast raster, search double precision[], exclude_nodata_value boolean DEFAULT true, OUT val double precision, OUT x integer, OUT y integer)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT val, x, y FROM public.ST_PixelOfValue($1, 1, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pixelofvalue(rast raster, nband integer, search double precision, exclude_nodata_value boolean DEFAULT true, OUT x integer, OUT y integer)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT x, y FROM public.ST_PixelofValue($1, $2, ARRAY[$3], $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pixelofvalue(rast raster, search double precision, exclude_nodata_value boolean DEFAULT true, OUT x integer, OUT y integer)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT x, y FROM public.ST_PixelOfValue($1, 1, ARRAY[$2], $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pixelofvalue(rast raster, nband integer, search double precision[], exclude_nodata_value boolean DEFAULT true, OUT val double precision, OUT x integer, OUT y integer)
 RETURNS SETOF record
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_pixelOfValue$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pixelwidth(raster)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getPixelWidth$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_point(double precision, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_makepoint$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_point_inside_circle(geometry, double precision, double precision, double precision)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Point_Inside_Circle', 'ST_PointInsideCircle', '2.2.0');
    SELECT public.ST_PointInsideCircle($1,$2,$3,$4);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pointfromgeohash(text, integer DEFAULT NULL::integer)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE
AS '$libdir/postgis-2.5', $function$point_from_geohash$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pointfromtext(text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromText($1)) = 'POINT'
	THEN public.ST_GeomFromText($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pointfromtext(text, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromText($1, $2)) = 'POINT'
	THEN public.ST_GeomFromText($1, $2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pointfromwkb(bytea)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1)) = 'POINT'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pointfromwkb(bytea, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1, $2)) = 'POINT'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pointinsidecircle(geometry, double precision, double precision, double precision)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_inside_circle_point$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pointn(geometry, integer)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_pointn_linestring$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_pointonsurface(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$pointonsurface$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_points(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_Points$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_polyfromtext(text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromText($1)) = 'POLYGON'
	THEN public.ST_GeomFromText($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_polyfromtext(text, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromText($1, $2)) = 'POLYGON'
	THEN public.ST_GeomFromText($1, $2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_polyfromwkb(bytea)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1)) = 'POLYGON'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_polyfromwkb(bytea, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1, $2)) = 'POLYGON'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_polygon(rast raster, band integer DEFAULT 1)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getPolygon$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_polygon(geometry, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT public.ST_SetSRID(public.ST_MakePolygon($1), $2)
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_polygonfromtext(text, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_PolyFromText($1, $2)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_polygonfromtext(text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_PolyFromText($1)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_polygonfromwkb(bytea)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1)) = 'POLYGON'
	THEN public.ST_GeomFromWKB($1)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_polygonfromwkb(bytea, integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
	SELECT CASE WHEN public.geometrytype(public.ST_GeomFromWKB($1,$2)) = 'POLYGON'
	THEN public.ST_GeomFromWKB($1, $2)
	ELSE NULL END
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_polygonize(geometry[])
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$polygonize_garray$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_project(geog geography, distance double precision, azimuth double precision)
 RETURNS geography
 LANGUAGE c
 IMMUTABLE COST 100
AS '$libdir/postgis-2.5', $function$geography_project$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_quantile(rast raster, quantile double precision)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT ( public._ST_quantile($1, 1, TRUE, 1, ARRAY[$2]::double precision[])).value $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_quantile(rastertable text, rastercolumn text, exclude_nodata_value boolean, quantile double precision DEFAULT NULL::double precision)
 RETURNS double precision
 LANGUAGE sql
 STABLE
AS $function$ SELECT ( public._ST_quantile($1, $2, 1, $3, 1, ARRAY[$4]::double precision[])).value $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_quantile(rastertable text, rastercolumn text, nband integer, quantile double precision)
 RETURNS double precision
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT ( public._ST_quantile($1, $2, $3, TRUE, 1, ARRAY[$4]::double precision[])).value $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_quantile(rastertable text, rastercolumn text, nband integer, exclude_nodata_value boolean, quantile double precision)
 RETURNS double precision
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT ( public._ST_quantile($1, $2, $3, $4, 1, ARRAY[$5]::double precision[])).value $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_quantile(rastertable text, rastercolumn text, quantiles double precision[], OUT quantile double precision, OUT value double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_quantile($1, $2, 1, TRUE, 1, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_quantile(rastertable text, rastercolumn text, nband integer, quantiles double precision[], OUT quantile double precision, OUT value double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_quantile($1, $2, $3, TRUE, 1, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_quantile(rastertable text, rastercolumn text, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, quantiles double precision[] DEFAULT NULL::double precision[], OUT quantile double precision, OUT value double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE
AS $function$ SELECT public._ST_quantile($1, $2, $3, $4, 1, $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_quantile(rast raster, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, quantiles double precision[] DEFAULT NULL::double precision[], OUT quantile double precision, OUT value double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_quantile($1, $2, $3, 1, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_quantile(rast raster, nband integer, quantiles double precision[], OUT quantile double precision, OUT value double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_quantile($1, $2, TRUE, 1, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_quantile(rast raster, exclude_nodata_value boolean, quantile double precision DEFAULT NULL::double precision)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT ( public._ST_quantile($1, 1, $2, 1, ARRAY[$3]::double precision[])).value $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_quantile(rast raster, quantiles double precision[], OUT quantile double precision, OUT value double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_quantile($1, 1, TRUE, 1, $2) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_quantile(rast raster, nband integer, exclude_nodata_value boolean, quantile double precision)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT ( public._ST_quantile($1, $2, $3, 1, ARRAY[$4]::double precision[])).value $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_quantile(rast raster, nband integer, quantile double precision)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT ( public._ST_quantile($1, $2, TRUE, 1, ARRAY[$3]::double precision[])).value $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_quantile(rastertable text, rastercolumn text, quantile double precision)
 RETURNS double precision
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT ( public._ST_quantile($1, $2, 1, TRUE, 1, ARRAY[$3]::double precision[])).value $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_quantizecoordinates(g geometry, prec_x integer, prec_y integer DEFAULT NULL::integer, prec_z integer DEFAULT NULL::integer, prec_m integer DEFAULT NULL::integer)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE COST 10
AS '$libdir/postgis-2.5', $function$ST_QuantizeCoordinates$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_range4ma(matrix double precision[], nodatamode text, VARIADIC args text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
    DECLARE
        _matrix float[][];
        min float;
        max float;
    BEGIN
        _matrix := matrix;
        min := 'Infinity'::float;
        max := '-Infinity'::float;
        FOR x in array_lower(matrix, 1)..array_upper(matrix, 1) LOOP
            FOR y in array_lower(matrix, 2)..array_upper(matrix, 2) LOOP
                IF _matrix[x][y] IS NULL THEN
                    IF NOT nodatamode = 'ignore' THEN
                        _matrix[x][y] := nodatamode::float;
                    END IF;
                END IF;
                IF min > _matrix[x][y] THEN
                    min = _matrix[x][y];
                END IF;
                IF max < _matrix[x][y] THEN
                    max = _matrix[x][y];
                END IF;
            END LOOP;
        END LOOP;
        IF max = '-Infinity'::float OR min = 'Infinity'::float THEN
            RETURN NULL;
        END IF;
        RETURN max - min;
    END;
    $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_range4ma(value double precision[], pos integer[], VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		_value double precision[][][];
		min double precision;
		max double precision;
		x int;
		y int;
		z int;
		ndims int;
	BEGIN
		min := 'Infinity'::double precision;
		max := '-Infinity'::double precision;

		ndims := array_ndims(value);
		-- add a third dimension if 2-dimension
		IF ndims = 2 THEN
			_value := public._ST_convertarray4ma(value);
		ELSEIF ndims != 3 THEN
			RAISE EXCEPTION 'First parameter of function must be a 3-dimension array';
		ELSE
			_value := value;
		END IF;

		-- raster
		FOR z IN array_lower(_value, 1)..array_upper(_value, 1) LOOP
			-- row
			FOR y IN array_lower(_value, 2)..array_upper(_value, 2) LOOP
				-- column
				FOR x IN array_lower(_value, 3)..array_upper(_value, 3) LOOP
					IF _value[z][y][x] IS NULL THEN
						IF array_length(userargs, 1) > 0 THEN
							_value[z][y][x] = userargs[array_lower(userargs, 1)]::double precision;
						ELSE
							CONTINUE;
						END IF;
					END IF;

					IF _value[z][y][x] < min THEN
						min := _value[z][y][x];
					END IF;
					IF _value[z][y][x] > max THEN
						max := _value[z][y][x];
					END IF;
				END LOOP;
			END LOOP;
		END LOOP;

		IF max = '-Infinity'::double precision OR min = 'Infinity'::double precision THEN
			RETURN NULL;
		END IF;

		RETURN max - min;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_rastertoworldcoord(rast raster, columnx integer, rowy integer, OUT longitude double precision, OUT latitude double precision)
 RETURNS record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT longitude, latitude FROM public._ST_rastertoworldcoord($1, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_rastertoworldcoordx(rast raster, xr integer, yr integer)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT longitude FROM public._ST_rastertoworldcoord($1, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_rastertoworldcoordx(rast raster, xr integer)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT longitude FROM public._ST_rastertoworldcoord($1, $2, NULL) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_rastertoworldcoordy(rast raster, xr integer, yr integer)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT latitude FROM public._ST_rastertoworldcoord($1, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_rastertoworldcoordy(rast raster, yr integer)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT latitude FROM public._ST_rastertoworldcoord($1, NULL, $2) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_rastfromhexwkb(text)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_fromHexWKB$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_rastfromwkb(bytea)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_fromWKB$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_reclass(rast raster, VARIADIC reclassargset reclassarg[])
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
	DECLARE
		i int;
		expr text;
	BEGIN
		-- for each reclassarg, validate elements as all except nodataval cannot be NULL
		FOR i IN SELECT * FROM generate_subscripts($2, 1) LOOP
			IF $2[i].nband IS NULL OR $2[i].reclassexpr IS NULL OR $2[i].pixeltype IS NULL THEN
				RAISE WARNING 'Values are required for the nband, reclassexpr and pixeltype attributes.';
				RETURN rast;
			END IF;
		END LOOP;

		RETURN public._ST_reclass($1, VARIADIC $2);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_reclass(rast raster, nband integer, reclassexpr text, pixeltype text, nodataval double precision DEFAULT NULL::double precision)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT st_reclass($1, ROW($2, $3, $4, $5)) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_reclass(rast raster, reclassexpr text, pixeltype text)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT st_reclass($1, ROW(1, $2, $3, NULL)) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_relate(geom1 geometry, geom2 geometry)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$relate_full$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_relate(geom1 geometry, geom2 geometry, integer)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$relate_full$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_relate(geom1 geometry, geom2 geometry, text)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$relate_pattern$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_relatematch(text, text)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$ST_RelateMatch$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_removepoint(geometry, integer)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_removepoint$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_removerepeatedpoints(geom geometry, tolerance double precision DEFAULT 0.0)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_RemoveRepeatedPoints$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_resample(rast raster, scalex double precision DEFAULT 0, scaley double precision DEFAULT 0, gridx double precision DEFAULT NULL::double precision, gridy double precision DEFAULT NULL::double precision, skewx double precision DEFAULT 0, skewy double precision DEFAULT 0, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_gdalwarp($1, $8,	$9, NULL, $2, $3, $4, $5, $6, $7) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_resample(rast raster, ref raster, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125, usescale boolean DEFAULT true)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
	DECLARE
		rastsrid int;

		_srid int;
		_dimx int;
		_dimy int;
		_scalex double precision;
		_scaley double precision;
		_gridx double precision;
		_gridy double precision;
		_skewx double precision;
		_skewy double precision;
	BEGIN
		SELECT srid, width, height, scalex, scaley, upperleftx, upperlefty, skewx, skewy INTO _srid, _dimx, _dimy, _scalex, _scaley, _gridx, _gridy, _skewx, _skewy FROM st_metadata($2);

		rastsrid := public.ST_SRID($1);

		-- both rasters must have the same SRID
		IF (rastsrid != _srid) THEN
			RAISE EXCEPTION 'The raster to be resampled has a different SRID from the reference raster';
			RETURN NULL;
		END IF;

		IF usescale IS TRUE THEN
			_dimx := NULL;
			_dimy := NULL;
		ELSE
			_scalex := NULL;
			_scaley := NULL;
		END IF;

		RETURN public._ST_gdalwarp($1, $3, $4, NULL, _scalex, _scaley, _gridx, _gridy, _skewx, _skewy, _dimx, _dimy);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_resample(rast raster, ref raster, usescale boolean, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.st_resample($1, $2, $4, $5, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_resample(rast raster, width integer, height integer, gridx double precision DEFAULT NULL::double precision, gridy double precision DEFAULT NULL::double precision, skewx double precision DEFAULT 0, skewy double precision DEFAULT 0, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_gdalwarp($1, $8,	$9, NULL, NULL, NULL, $4, $5, $6, $7, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_rescale(rast raster, scalexy double precision, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT  public._ST_GdalWarp($1, $3, $4, NULL, $2, $2) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_rescale(rast raster, scalex double precision, scaley double precision, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT  public._ST_GdalWarp($1, $4, $5, NULL, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_resize(rast raster, width integer, height integer, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_gdalwarp($1, $4, $5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, abs($2), abs($3)) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_resize(rast raster, percentwidth double precision, percentheight double precision, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
	DECLARE
		_width integer;
		_height integer;
	BEGIN
		-- range check
		IF $2 <= 0. OR $2 > 1. OR $3 <= 0. OR $3 > 1. THEN
			RAISE EXCEPTION 'Percentages must be a value greater than zero and less than or equal to one, e.g. 0.5 for 50%%';
		END IF;

		SELECT width, height INTO _width, _height FROM public.ST_Metadata($1);

		_width := round(_width::double precision * $2)::integer;
		_height:= round(_height::double precision * $3)::integer;

		IF _width < 1 THEN
			_width := 1;
		END IF;
		IF _height < 1 THEN
			_height := 1;
		END IF;

		RETURN public._ST_gdalwarp(
			$1,
			$4, $5,
			NULL,
			NULL, NULL,
			NULL, NULL,
			NULL, NULL,
			_width, _height
		);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_resize(rast raster, width text, height text, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
	DECLARE
		i integer;

		wh text[2];

		whi integer[2];
		whd double precision[2];

		_width integer;
		_height integer;
	BEGIN
		wh[1] := trim(both from $2);
		wh[2] := trim(both from $3);

		-- see if width and height are percentages
		FOR i IN 1..2 LOOP
			IF position('%' in wh[i]) > 0 THEN
				BEGIN
					wh[i] := (regexp_matches(wh[i], E'^(\\d*.?\\d*)%{1}$'))[1];
					IF length(wh[i]) < 1 THEN
						RAISE invalid_parameter_value;
					END IF;

					whd[i] := wh[i]::double precision * 0.01;
				EXCEPTION WHEN OTHERS THEN -- TODO: WHEN invalid_parameter_value !
					RAISE EXCEPTION 'Invalid percentage value provided for width/height';
					RETURN NULL;
				END;
			ELSE
				BEGIN
					whi[i] := abs(wh[i]::integer);
				EXCEPTION WHEN OTHERS THEN -- TODO: only handle appropriate SQLSTATE
					RAISE EXCEPTION 'Non-integer value provided for width/height';
					RETURN NULL;
				END;
			END IF;
		END LOOP;

		IF whd[1] IS NOT NULL OR whd[2] IS NOT NULL THEN
			SELECT foo.width, foo.height INTO _width, _height FROM public.ST_Metadata($1) AS foo;

			IF whd[1] IS NOT NULL THEN
				whi[1] := round(_width::double precision * whd[1])::integer;
			END IF;

			IF whd[2] IS NOT NULL THEN
				whi[2] := round(_height::double precision * whd[2])::integer;
			END IF;

		END IF;

		-- should NEVER be here
		IF whi[1] IS NULL OR whi[2] IS NULL THEN
			RAISE EXCEPTION 'Unable to determine appropriate width or height';
			RETURN NULL;
		END IF;

		FOR i IN 1..2 LOOP
			IF whi[i] < 1 THEN
				whi[i] = 1;
			END IF;
		END LOOP;

		RETURN public._ST_gdalwarp(
			$1,
			$4, $5,
			NULL,
			NULL, NULL,
			NULL, NULL,
			NULL, NULL,
			whi[1], whi[2]
		);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_reskew(rast raster, skewxy double precision, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_GdalWarp($1, $3, $4, NULL, 0, 0, NULL, NULL, $2, $2) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_reskew(rast raster, skewx double precision, skewy double precision, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_GdalWarp($1, $4, $5, NULL, 0, 0, NULL, NULL, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_retile(tab regclass, col name, ext geometry, sfx double precision, sfy double precision, tw integer, th integer, algo text DEFAULT 'NearestNeighbour'::text)
 RETURNS SETOF raster
 LANGUAGE plpgsql
 STABLE STRICT
AS $function$
DECLARE
  rec RECORD;
  ipx FLOAT8;
  ipy FLOAT8;
  tx int;
  ty int;
  te public.GEOMETRY; -- tile extent
  ncols int;
  nlins int;
  srid int;
  sql TEXT;
BEGIN

  RAISE DEBUG 'Target coverage will have sfx=%, sfy=%', sfx, sfy;

  -- 2. Loop over each target tile and build it from source tiles
  ipx := public.st_xmin(ext);
  ncols := ceil((st_xmax(ext)-ipx)/sfx/tw);
  IF sfy < 0 THEN
    ipy := public.st_ymax(ext);
    nlins := ceil((public.st_ymin(ext)-ipy)/sfy/th);
  ELSE
    ipy := public.st_ymin(ext);
    nlins := ceil((public.st_ymax(ext)-ipy)/sfy/th);
  END IF;

  srid := public.ST_Srid(ext);

  RAISE DEBUG 'Target coverage will have % x % tiles, each of approx size % x %', ncols, nlins, tw, th;
  RAISE DEBUG 'Target coverage will cover extent %', ext::box2d;

  FOR tx IN 0..ncols-1 LOOP
    FOR ty IN 0..nlins-1 LOOP
      te := public.ST_MakeEnvelope(ipx + tx     *  tw  * sfx,
                             ipy + ty     *  th  * sfy,
                             ipx + (tx+1) *  tw  * sfx,
                             ipy + (ty+1) *  th  * sfy,
                             srid);
      --RAISE DEBUG 'sfx/sfy: %, %', sfx, sfy;
      --RAISE DEBUG 'tile extent %', te;
      sql := 'SELECT count(*),  public.ST_Clip(  public.ST_Union(  public.ST_SnapToGrid(  public.ST_Rescale(  public.ST_Clip(' || quote_ident(col)
          || ',  public.ST_Expand($3, greatest($1,$2))),$1, $2, $6), $4, $5, $1, $2)), $3) g FROM ' || tab::text
          || ' WHERE  public.ST_Intersects(' || quote_ident(col) || ', $3)';
      --RAISE DEBUG 'SQL: %', sql;
      FOR rec IN EXECUTE sql USING sfx, sfy, te, ipx, ipy, algo LOOP
        --RAISE DEBUG '% source tiles intersect target tile %,% with extent %', rec.count, tx, ty, te::box2d;
        IF rec.g IS NULL THEN
          RAISE WARNING 'No source tiles cover target tile %,% with extent %',
            tx, ty, te::box2d;
        ELSE
          --RAISE DEBUG 'Tile for extent % has size % x %', te::box2d, st_width(rec.g), st_height(rec.g);
          RETURN NEXT rec.g;
        END IF;
      END LOOP;
    END LOOP;
  END LOOP;

  RETURN;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_reverse(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_reverse$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_rotate(geometry, double precision, geometry)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_Affine($1,  cos($2), -sin($2), 0,  sin($2),  cos($2), 0, 0, 0, 1, public.ST_X($3) - cos($2) * public.ST_X($3) + sin($2) * public.ST_Y($3), public.ST_Y($3) - sin($2) * public.ST_X($3) - cos($2) * public.ST_Y($3), 0)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_rotate(geometry, double precision)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_Affine($1,  cos($2), -sin($2), 0,  sin($2), cos($2), 0,  0, 0, 1,  0, 0, 0)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_rotate(geometry, double precision, double precision, double precision)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_Affine($1,  cos($2), -sin($2), 0,  sin($2),  cos($2), 0, 0, 0, 1,	$3 - cos($2) * $3 + sin($2) * $4, $4 - sin($2) * $3 - cos($2) * $4, 0)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_rotatex(geometry, double precision)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_Affine($1, 1, 0, 0, 0, cos($2), -sin($2), 0, sin($2), cos($2), 0, 0, 0)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_rotatey(geometry, double precision)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_Affine($1,  cos($2), 0, sin($2),  0, 1, 0,  -sin($2), 0, cos($2), 0,  0, 0)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_rotatez(geometry, double precision)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_Rotate($1, $2)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_rotation(raster)
 RETURNS double precision
 LANGUAGE sql
AS $function$ SELECT ( public.ST_Geotransform($1)).theta_i $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_roughness(rast raster, nband integer, customextent raster, pixeltype text DEFAULT '32BF'::text, interpolate_nodata boolean DEFAULT false)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		_rast public.raster;
		_nband integer;
		_pixtype text;
		_pixwidth double precision;
		_pixheight double precision;
		_width integer;
		_height integer;
		_customextent public.raster;
		_extenttype text;
	BEGIN
		_customextent := customextent;
		IF _customextent IS NULL THEN
			_extenttype := 'FIRST';
		ELSE
			_extenttype := 'CUSTOM';
		END IF;

		IF interpolate_nodata IS TRUE THEN
			_rast := public.ST_MapAlgebra(
				ARRAY[ROW(rast, nband)]::rastbandarg[],
				'public.st_invdistweight4ma(double precision[][][], integer[][], text[])'::regprocedure,
				pixeltype,
				'FIRST', NULL,
				1, 1
			);
			_nband := 1;
			_pixtype := NULL;
		ELSE
			_rast := rast;
			_nband := nband;
			_pixtype := pixeltype;
		END IF;

		RETURN public.ST_MapAlgebra(
			ARRAY[ROW(_rast, _nband)]::rastbandarg[],
			' public._ST_roughness4ma(double precision[][][], integer[][], text[])'::regprocedure,
			_pixtype,
			_extenttype, _customextent,
			1, 1);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_roughness(rast raster, nband integer DEFAULT 1, pixeltype text DEFAULT '32BF'::text, interpolate_nodata boolean DEFAULT false)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_roughness($1, $2, NULL::public.raster, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_samealignment(ulx1 double precision, uly1 double precision, scalex1 double precision, scaley1 double precision, skewx1 double precision, skewy1 double precision, ulx2 double precision, uly2 double precision, scalex2 double precision, scaley2 double precision, skewx2 double precision, skewy2 double precision)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT st_samealignment(st_makeemptyraster(1, 1, $1, $2, $3, $4, $5, $6), st_makeemptyraster(1, 1, $7, $8, $9, $10, $11, $12)) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_samealignment(rast1 raster, rast2 raster)
 RETURNS boolean
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_sameAlignment$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_scale(geometry, double precision, double precision)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_Scale($1, $2, $3, 1)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_scale(geometry, double precision, double precision, double precision)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_Scale($1, public.ST_MakePoint($2, $3, $4))$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_scale(geometry, geometry, origin geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_Scale$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_scale(geometry, geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_Scale$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_scalex(raster)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getXScale$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_scaley(raster)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getYScale$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_segmentize(geometry, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_segmentize2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_segmentize(geog geography, max_segment_length double precision)
 RETURNS geography
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$geography_segmentize$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setbandindex(rast raster, band integer, outdbindex integer, force boolean DEFAULT false)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_SetBandPath($1, $2, NULL, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setbandisnodata(rast raster, band integer DEFAULT 1)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_setBandIsNoData$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setbandnodatavalue(rast raster, band integer, nodatavalue double precision, forcechecking boolean DEFAULT false)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_setBandNoDataValue$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setbandnodatavalue(rast raster, nodatavalue double precision)
 RETURNS raster
 LANGUAGE sql
AS $function$ SELECT public.ST_setbandnodatavalue($1, 1, $2, FALSE) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setbandpath(rast raster, band integer, outdbpath text, outdbindex integer, force boolean DEFAULT false)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_setBandPath$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_seteffectivearea(geometry, double precision DEFAULT '-1'::integer, integer DEFAULT 1)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_SetEffectiveArea$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setgeoreference(rast raster, georef text, format text DEFAULT 'GDAL'::text)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
    DECLARE
        params text[];
        rastout public.raster;
    BEGIN
        IF rast IS NULL THEN
            RAISE WARNING 'Cannot set georeferencing on a null raster in st_setgeoreference.';
            RETURN rastout;
        END IF;

        SELECT regexp_matches(georef,
            E'(-?\\d+(?:\\.\\d+)?)\\s(-?\\d+(?:\\.\\d+)?)\\s(-?\\d+(?:\\.\\d+)?)\\s' ||
            E'(-?\\d+(?:\\.\\d+)?)\\s(-?\\d+(?:\\.\\d+)?)\\s(-?\\d+(?:\\.\\d+)?)') INTO params;

        IF NOT FOUND THEN
            RAISE EXCEPTION 'st_setgeoreference requires a string with 6 floating point values.';
        END IF;

        IF format = 'ESRI' THEN
            -- params array is now:
            -- {scalex, skewy, skewx, scaley, upperleftx, upperlefty}
            rastout := public.ST_setscale(rast, params[1]::float8, params[4]::float8);
            rastout := public.ST_setskew(rastout, params[3]::float8, params[2]::float8);
            rastout := public.ST_setupperleft(rastout,
                                   params[5]::float8 - (params[1]::float8 * 0.5),
                                   params[6]::float8 - (params[4]::float8 * 0.5));
        ELSE
            IF format != 'GDAL' THEN
                RAISE WARNING 'Format ''%'' is not recognized, defaulting to GDAL format.', format;
            END IF;
            -- params array is now:
            -- {scalex, skewy, skewx, scaley, upperleftx, upperlefty}

            rastout := public.ST_setscale(rast, params[1]::float8, params[4]::float8);
            rastout := public.ST_setskew( rastout, params[3]::float8, params[2]::float8);
            rastout := public.ST_setupperleft(rastout, params[5]::float8, params[6]::float8);
        END IF;
        RETURN rastout;
    END;
    $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setgeoreference(rast raster, upperleftx double precision, upperlefty double precision, scalex double precision, scaley double precision, skewx double precision, skewy double precision)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_setgeoreference($1, array_to_string(ARRAY[$4, $7, $6, $5, $2, $3], ' ')) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setgeotransform(rast raster, imag double precision, jmag double precision, theta_i double precision, theta_ij double precision, xoffset double precision, yoffset double precision)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_setGeotransform$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setpoint(geometry, integer, geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_setpoint_linestring$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setrotation(rast raster, rotation double precision)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_setRotation$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setscale(rast raster, scalex double precision, scaley double precision)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_setScaleXY$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setscale(rast raster, scale double precision)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_setScale$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setskew(rast raster, skewx double precision, skewy double precision)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_setSkewXY$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setskew(rast raster, skew double precision)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_setSkew$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setsrid(geometry, integer)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_set_srid$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setsrid(geog geography, srid integer)
 RETURNS geography
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_set_srid$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setsrid(rast raster, srid integer)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_setSRID$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setupperleft(rast raster, upperleftx double precision, upperlefty double precision)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_setUpperLeftXY$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setvalue(rast raster, x integer, y integer, newvalue double precision)
 RETURNS raster
 LANGUAGE sql
AS $function$ SELECT public.ST_SetValue($1, 1, $2, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setvalue(rast raster, band integer, x integer, y integer, newvalue double precision)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_setPixelValue$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setvalue(rast raster, nband integer, geom geometry, newvalue double precision)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_setvalues($1, $2, ARRAY[ROW($3, $4)]::geomval[], FALSE) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setvalue(rast raster, geom geometry, newvalue double precision)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_setvalues($1, 1, ARRAY[ROW($2, $3)]::geomval[], FALSE) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setvalues(rast raster, nband integer, x integer, y integer, newvalueset double precision[], nosetvalue double precision, keepnodata boolean DEFAULT false)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_setvalues($1, $2, $3, $4, $5, NULL, TRUE, $6, $7) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setvalues(rast raster, nband integer, geomvalset geomval[], keepnodata boolean DEFAULT false)
 RETURNS raster
 LANGUAGE c
 IMMUTABLE
AS '$libdir/rtpostgis-2.5', $function$RASTER_setPixelValuesGeomval$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setvalues(rast raster, x integer, y integer, width integer, height integer, newvalue double precision, keepnodata boolean DEFAULT false)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	BEGIN
		IF width <= 0 OR height <= 0 THEN
			RAISE EXCEPTION 'Values for width and height must be greater than zero';
			RETURN NULL;
		END IF;
		RETURN public._ST_setvalues($1, 1, $2, $3, array_fill($6, ARRAY[$5, $4]::int[]), NULL, FALSE, NULL, $7);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setvalues(rast raster, nband integer, x integer, y integer, width integer, height integer, newvalue double precision, keepnodata boolean DEFAULT false)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	BEGIN
		IF width <= 0 OR height <= 0 THEN
			RAISE EXCEPTION 'Values for width and height must be greater than zero';
			RETURN NULL;
		END IF;
		RETURN public._ST_setvalues($1, $2, $3, $4, array_fill($7, ARRAY[$6, $5]::int[]), NULL, FALSE, NULL, $8);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_setvalues(rast raster, nband integer, x integer, y integer, newvalueset double precision[], noset boolean[] DEFAULT NULL::boolean[], keepnodata boolean DEFAULT false)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_setvalues($1, $2, $3, $4, $5, $6, FALSE, NULL, $7) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_sharedpaths(geom1 geometry, geom2 geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_SharedPaths$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_shift_longitude(geometry)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._postgis_deprecate('ST_Shift_Longitude', 'ST_ShiftLongitude', '2.2.0');
    SELECT public.ST_ShiftLongitude($1);
  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_shiftlongitude(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_longitude_shift$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_shortestline(geom1 geometry, geom2 geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_shortestline2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_simplify(geometry, double precision, boolean)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_simplify2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_simplify(geometry, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_simplify2d$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_simplifypreservetopology(geometry, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$topologypreservesimplify$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_simplifyvw(geometry, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_SetEffectiveArea$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_skewx(raster)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getXSkew$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_skewy(raster)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getYSkew$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_slope(rast raster, nband integer, customextent raster, pixeltype text DEFAULT '32BF'::text, units text DEFAULT 'DEGREES'::text, scale double precision DEFAULT 1.0, interpolate_nodata boolean DEFAULT false)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		_rast public.raster;
		_nband integer;
		_pixtype text;
		_pixwidth double precision;
		_pixheight double precision;
		_width integer;
		_height integer;
		_customextent public.raster;
		_extenttype text;
	BEGIN
		_customextent := customextent;
		IF _customextent IS NULL THEN
			_extenttype := 'FIRST';
		ELSE
			_extenttype := 'CUSTOM';
		END IF;

		IF interpolate_nodata IS TRUE THEN
			_rast := public.ST_MapAlgebra(
				ARRAY[ROW(rast, nband)]::rastbandarg[],
				'public.st_invdistweight4ma(double precision[][][], integer[][], text[])'::regprocedure,
				pixeltype,
				'FIRST', NULL,
				1, 1
			);
			_nband := 1;
			_pixtype := NULL;
		ELSE
			_rast := rast;
			_nband := nband;
			_pixtype := pixeltype;
		END IF;

		-- get properties
		_pixwidth := public.ST_PixelWidth(_rast);
		_pixheight := public.ST_PixelHeight(_rast);
		SELECT width, height INTO _width, _height FROM public.ST_Metadata(_rast);

		RETURN public.ST_MapAlgebra(
			ARRAY[ROW(_rast, _nband)]::rastbandarg[],
			' public._ST_slope4ma(double precision[][][], integer[][], text[])'::regprocedure,
			_pixtype,
			_extenttype, _customextent,
			1, 1,
			_pixwidth::text, _pixheight::text,
			_width::text, _height::text,
			units::text, scale::text
		);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_slope(rast raster, nband integer DEFAULT 1, pixeltype text DEFAULT '32BF'::text, units text DEFAULT 'DEGREES'::text, scale double precision DEFAULT 1.0, interpolate_nodata boolean DEFAULT false)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_slope($1, $2, NULL::public.raster, $3, $4, $5, $6) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_snap(geom1 geometry, geom2 geometry, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_Snap$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_snaptogrid(geometry, double precision, double precision)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT COST 1
AS $function$SELECT public.ST_SnapToGrid($1, 0, 0, $2, $3)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_snaptogrid(rast raster, gridx double precision, gridy double precision, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125, scalex double precision DEFAULT 0, scaley double precision DEFAULT 0)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_GdalWarp($1, $4, $5, NULL, $6, $7, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_snaptogrid(rast raster, gridx double precision, gridy double precision, scalex double precision, scaley double precision, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_gdalwarp($1, $6, $7, NULL, $4, $5, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_snaptogrid(rast raster, gridx double precision, gridy double precision, scalexy double precision, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_gdalwarp($1, $5, $6, NULL, $4, $4, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_snaptogrid(geom1 geometry, geom2 geometry, double precision, double precision, double precision, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_snaptogrid_pointoff$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_snaptogrid(geometry, double precision)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_SnapToGrid($1, 0, 0, $2, $2)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_snaptogrid(geometry, double precision, double precision, double precision, double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_snaptogrid$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_split(geom1 geometry, geom2 geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_Split$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_srid(geometry)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT COST 5
AS '$libdir/postgis-2.5', $function$LWGEOM_get_srid$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_srid(raster)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getSRID$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_srid(geog geography)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_get_srid$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_startpoint(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_startpoint_linestring$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_stddev4ma(value double precision[], pos integer[], VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT stddev(unnest) FROM unnest($1) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_stddev4ma(matrix double precision[], nodatamode text, VARIADIC args text[])
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT stddev(unnest) FROM unnest($1) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_subdivide(geom geometry, maxvertices integer DEFAULT 256)
 RETURNS SETOF geometry
 LANGUAGE c
 IMMUTABLE STRICT COST 100
AS '$libdir/postgis-2.5', $function$ST_Subdivide$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_sum4ma(matrix double precision[], nodatamode text, VARIADIC args text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
    DECLARE
        _matrix float[][];
        sum float;
    BEGIN
        _matrix := matrix;
        sum := 0;
        FOR x in array_lower(matrix, 1)..array_upper(matrix, 1) LOOP
            FOR y in array_lower(matrix, 2)..array_upper(matrix, 2) LOOP
                IF _matrix[x][y] IS NULL THEN
                    IF nodatamode = 'ignore' THEN
                        _matrix[x][y] := 0;
                    ELSE
                        _matrix[x][y] := nodatamode::float;
                    END IF;
                END IF;
                sum := sum + _matrix[x][y];
            END LOOP;
        END LOOP;
        RETURN sum;
    END;
    $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_sum4ma(value double precision[], pos integer[], VARIADIC userargs text[] DEFAULT NULL::text[])
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		_value double precision[][][];
		sum double precision;
		x int;
		y int;
		z int;
		ndims int;
	BEGIN
		sum := 0;

		ndims := array_ndims(value);
		-- add a third dimension if 2-dimension
		IF ndims = 2 THEN
			_value := public._ST_convertarray4ma(value);
		ELSEIF ndims != 3 THEN
			RAISE EXCEPTION 'First parameter of function must be a 3-dimension array';
		ELSE
			_value := value;
		END IF;

		-- raster
		FOR z IN array_lower(_value, 1)..array_upper(_value, 1) LOOP
			-- row
			FOR y IN array_lower(_value, 2)..array_upper(_value, 2) LOOP
				-- column
				FOR x IN array_lower(_value, 3)..array_upper(_value, 3) LOOP
					IF _value[z][y][x] IS NULL THEN
						IF array_length(userargs, 1) > 0 THEN
							_value[z][y][x] = userargs[array_lower(userargs, 1)]::double precision;
						ELSE
							CONTINUE;
						END IF;
					END IF;

					sum := sum + _value[z][y][x];
				END LOOP;
			END LOOP;
		END LOOP;

		RETURN sum;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_summary(geography)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_summary$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_summary(rast raster)
 RETURNS text
 LANGUAGE plpgsql
 STABLE STRICT
AS $function$
	DECLARE
		extent box2d;
		metadata record;
		bandmetadata record;
		msg text;
		msgset text[];
	BEGIN
		extent := public.ST_Extent(rast::public.geometry);
		metadata := public.ST_Metadata(rast);

		msg := 'Raster of ' || metadata.width || 'x' || metadata.height || ' pixels has ' || metadata.numbands || ' ';

		IF metadata.numbands = 1 THEN
			msg := msg || 'band ';
		ELSE
			msg := msg || 'bands ';
		END IF;
		msg := msg || 'and extent of ' || extent;

		IF
			round(metadata.skewx::numeric, 10) <> round(0::numeric, 10) OR
			round(metadata.skewy::numeric, 10) <> round(0::numeric, 10)
		THEN
			msg := 'Skewed ' || overlay(msg placing 'r' from 1 for 1);
		END IF;

		msgset := Array[]::text[] || msg;

		FOR bandmetadata IN SELECT * FROM public.ST_BandMetadata(rast, ARRAY[]::int[]) LOOP
			msg := 'band ' || bandmetadata.bandnum || ' of pixtype ' || bandmetadata.pixeltype || ' is ';
			IF bandmetadata.isoutdb IS FALSE THEN
				msg := msg || 'in-db ';
			ELSE
				msg := msg || 'out-db ';
			END IF;

			msg := msg || 'with ';
			IF bandmetadata.nodatavalue IS NOT NULL THEN
				msg := msg || 'NODATA value of ' || bandmetadata.nodatavalue;
			ELSE
				msg := msg || 'no NODATA value';
			END IF;

			msgset := msgset || ('    ' || msg);
		END LOOP;

		RETURN array_to_string(msgset, E'\n');
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_summary(geometry)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT COST 25
AS '$libdir/postgis-2.5', $function$LWGEOM_summary$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_summarystats(rastertable text, rastercolumn text, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true)
 RETURNS summarystats
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_summarystats($1, $2, $3, $4, 1) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_summarystats(rast raster, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true)
 RETURNS summarystats
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_summarystats($1, $2, $3, 1) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_summarystats(rast raster, exclude_nodata_value boolean)
 RETURNS summarystats
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_summarystats($1, 1, $2, 1) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_summarystats(rastertable text, rastercolumn text, exclude_nodata_value boolean)
 RETURNS summarystats
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT public._ST_summarystats($1, $2, 1, $3, 1) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_swapordinates(geom geometry, ords cstring)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_SwapOrdinates$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_symdifference(geom1 geometry, geom2 geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$symdifference$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_symmetricdifference(geom1 geometry, geom2 geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$symdifference$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_tile(rast raster, nband integer, width integer, height integer, padwithnodata boolean DEFAULT false, nodataval double precision DEFAULT NULL::double precision)
 RETURNS SETOF raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_tile($1, $3, $4, ARRAY[$2]::integer[], $5, $6) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_tile(rast raster, nband integer[], width integer, height integer, padwithnodata boolean DEFAULT false, nodataval double precision DEFAULT NULL::double precision)
 RETURNS SETOF raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_tile($1, $3, $4, $2, $5, $6) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_tile(rast raster, width integer, height integer, padwithnodata boolean DEFAULT false, nodataval double precision DEFAULT NULL::double precision)
 RETURNS SETOF raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public._ST_tile($1, $2, $3, NULL::integer[], $4, $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_touches(rast1 raster, nband1 integer, rast2 raster, nband2 integer)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT $1 OPERATOR(public.&&) $3 AND CASE WHEN $2 IS NULL OR $4 IS NULL THEN public._st_touches(public.st_convexhull($1), public.st_convexhull($3)) ELSE public._st_touches($1, $2, $3, $4) END $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_touches(rast1 raster, rast2 raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT public.st_touches($1, NULL::integer, $2, NULL::integer) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_touches(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $1 OPERATOR(public.&&) $2 AND public._ST_Touches($1,$2)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_tpi(rast raster, nband integer, customextent raster, pixeltype text DEFAULT '32BF'::text, interpolate_nodata boolean DEFAULT false)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		_rast public.raster;
		_nband integer;
		_pixtype text;
		_pixwidth double precision;
		_pixheight double precision;
		_width integer;
		_height integer;
		_customextent public.raster;
		_extenttype text;
	BEGIN
		_customextent := customextent;
		IF _customextent IS NULL THEN
			_extenttype := 'FIRST';
		ELSE
			_extenttype := 'CUSTOM';
		END IF;

		IF interpolate_nodata IS TRUE THEN
			_rast := public.ST_MapAlgebra(
				ARRAY[ROW(rast, nband)]::rastbandarg[],
				'public.st_invdistweight4ma(double precision[][][], integer[][], text[])'::regprocedure,
				pixeltype,
				'FIRST', NULL,
				1, 1
			);
			_nband := 1;
			_pixtype := NULL;
		ELSE
			_rast := rast;
			_nband := nband;
			_pixtype := pixeltype;
		END IF;

		-- get properties
		_pixwidth := public.ST_PixelWidth(_rast);
		_pixheight := public.ST_PixelHeight(_rast);
		SELECT width, height INTO _width, _height FROM public.ST_Metadata(_rast);

		RETURN public.ST_MapAlgebra(
			ARRAY[ROW(_rast, _nband)]::rastbandarg[],
			' public._ST_tpi4ma(double precision[][][], integer[][], text[])'::regprocedure,
			_pixtype,
			_extenttype, _customextent,
			1, 1);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_tpi(rast raster, nband integer DEFAULT 1, pixeltype text DEFAULT '32BF'::text, interpolate_nodata boolean DEFAULT false)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_tpi($1, $2, NULL::public.raster, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_transform(geom geometry, to_proj text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.postgis_transform_geometry($1, proj4text, $2, 0)
FROM spatial_ref_sys WHERE srid=public.ST_SRID($1);$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_transform(geometry, integer)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$transform$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_transform(geom geometry, from_proj text, to_srid integer)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.postgis_transform_geometry($1, $2, proj4text, $3)
FROM spatial_ref_sys WHERE srid=$3;$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_transform(geom geometry, from_proj text, to_proj text)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.postgis_transform_geometry($1, $2, $3, 0)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_transform(rast raster, alignto raster, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
	DECLARE
		_srid integer;
		_scalex double precision;
		_scaley double precision;
		_gridx double precision;
		_gridy double precision;
		_skewx double precision;
		_skewy double precision;
	BEGIN
		SELECT srid, scalex, scaley, upperleftx, upperlefty, skewx, skewy INTO _srid, _scalex, _scaley, _gridx, _gridy, _skewx, _skewy FROM st_metadata($2);

		RETURN public._ST_gdalwarp($1, $3, $4, _srid, _scalex, _scaley, _gridx, _gridy, _skewx, _skewy, NULL, NULL);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_transform(rast raster, srid integer, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125, scalex double precision DEFAULT 0, scaley double precision DEFAULT 0)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_gdalwarp($1, $3, $4, $2, $5, $6) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_transform(rast raster, srid integer, scalexy double precision, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_gdalwarp($1, $4, $5, $2, $3, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_transform(rast raster, srid integer, scalex double precision, scaley double precision, algorithm text DEFAULT 'NearestNeighbour'::text, maxerr double precision DEFAULT 0.125)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public._ST_gdalwarp($1, $5, $6, $2, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_translate(geometry, double precision, double precision, double precision)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_Affine($1, 1, 0, 0, 0, 1, 0, 0, 0, 1, $2, $3, $4)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_translate(geometry, double precision, double precision)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_Translate($1, $2, $3, 0)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_transscale(geometry, double precision, double precision, double precision, double precision)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$SELECT public.ST_Affine($1,  $4, 0, 0,  0, $5, 0,
		0, 0, 1,  $2 * $4, $3 * $5, 0)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_tri(rast raster, nband integer DEFAULT 1, pixeltype text DEFAULT '32BF'::text, interpolate_nodata boolean DEFAULT false)
 RETURNS raster
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT public.ST_tri($1, $2, NULL::public.raster, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_tri(rast raster, nband integer, customextent raster, pixeltype text DEFAULT '32BF'::text, interpolate_nodata boolean DEFAULT false)
 RETURNS raster
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE
		_rast public.raster;
		_nband integer;
		_pixtype text;
		_pixwidth double precision;
		_pixheight double precision;
		_width integer;
		_height integer;
		_customextent public.raster;
		_extenttype text;
	BEGIN
		_customextent := customextent;
		IF _customextent IS NULL THEN
			_extenttype := 'FIRST';
		ELSE
			_extenttype := 'CUSTOM';
		END IF;

		IF interpolate_nodata IS TRUE THEN
			_rast := public.ST_MapAlgebra(
				ARRAY[ROW(rast, nband)]::rastbandarg[],
				'public.st_invdistweight4ma(double precision[][][], integer[][], text[])'::regprocedure,
				pixeltype,
				'FIRST', NULL,
				1, 1
			);
			_nband := 1;
			_pixtype := NULL;
		ELSE
			_rast := rast;
			_nband := nband;
			_pixtype := pixeltype;
		END IF;

		-- get properties
		_pixwidth := public.ST_PixelWidth(_rast);
		_pixheight := public.ST_PixelHeight(_rast);
		SELECT width, height INTO _width, _height FROM public.ST_Metadata(_rast);

		RETURN public.ST_MapAlgebra(
			ARRAY[ROW(_rast, _nband)]::rastbandarg[],
			' public._ST_tri4ma(double precision[][][], integer[][], text[])'::regprocedure,
			_pixtype,
			_extenttype, _customextent,
			1, 1);
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_unaryunion(geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_UnaryUnion$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_union(geometry[])
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$pgis_union_geometry_array$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_union(geom1 geometry, geom2 geometry)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$geomunion$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_upperleftx(raster)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getXUpperLeft$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_upperlefty(raster)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getYUpperLeft$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_value(rast raster, pt geometry, exclude_nodata_value boolean DEFAULT true)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT public.ST_value($1, 1, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_value(rast raster, x integer, y integer, exclude_nodata_value boolean DEFAULT true)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT st_value($1, 1, $2, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_value(rast raster, band integer, pt geometry, exclude_nodata_value boolean DEFAULT true)
 RETURNS double precision
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
    DECLARE
        x float8;
        y float8;
        gtype text;
    BEGIN
        gtype := public.ST_GeometryType(pt);
        IF ( gtype != 'ST_Point' ) THEN
            RAISE EXCEPTION 'Attempting to get the value of a pixel with a non-point geometry';
        END IF;

				IF public.ST_SRID(pt) != public.ST_SRID(rast) THEN
            RAISE EXCEPTION 'Raster and geometry do not have the same SRID';
				END IF;

        x := public.ST_x(pt);
        y := public.ST_y(pt);
        RETURN public.ST_value(rast,
                        band,
                        public.ST_worldtorastercoordx(rast, x, y),
                        public.ST_worldtorastercoordy(rast, x, y),
                        exclude_nodata_value);
    END;
    $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_value(rast raster, band integer, x integer, y integer, exclude_nodata_value boolean DEFAULT true)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getPixelValue$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuecount(rast raster, nband integer, searchvalue double precision, roundto double precision DEFAULT 0)
 RETURNS integer
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT ( public._ST_valuecount($1, $2, TRUE, ARRAY[$3]::double precision[], $4)).count $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuecount(rast raster, nband integer, searchvalues double precision[], roundto double precision DEFAULT 0, OUT value double precision, OUT count integer)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT value, count FROM public._ST_valuecount($1, $2, TRUE, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuecount(rast raster, nband integer, exclude_nodata_value boolean, searchvalue double precision, roundto double precision DEFAULT 0)
 RETURNS integer
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT ( public._ST_valuecount($1, $2, $3, ARRAY[$4]::double precision[], $5)).count $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuecount(rastertable text, rastercolumn text, searchvalues double precision[], roundto double precision DEFAULT 0, OUT value double precision, OUT count integer)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE
AS $function$ SELECT value, count FROM public._ST_valuecount($1, $2, 1, TRUE, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuecount(rastertable text, rastercolumn text, nband integer, searchvalues double precision[], roundto double precision DEFAULT 0, OUT value double precision, OUT count integer)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE
AS $function$ SELECT value, count FROM public._ST_valuecount($1, $2, $3, TRUE, $4, $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuecount(rast raster, searchvalues double precision[], roundto double precision DEFAULT 0, OUT value double precision, OUT count integer)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT value, count FROM public._ST_valuecount($1, 1, TRUE, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuecount(rast raster, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, searchvalues double precision[] DEFAULT NULL::double precision[], roundto double precision DEFAULT 0, OUT value double precision, OUT count integer)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT value, count FROM public._ST_valuecount($1, $2, $3, $4, $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuecount(rastertable text, rastercolumn text, searchvalue double precision, roundto double precision DEFAULT 0)
 RETURNS integer
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT ( public._ST_valuecount($1, $2, 1, TRUE, ARRAY[$3]::double precision[], $4)).count $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuecount(rastertable text, rastercolumn text, nband integer, exclude_nodata_value boolean, searchvalue double precision, roundto double precision DEFAULT 0)
 RETURNS integer
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT ( public._ST_valuecount($1, $2, $3, $4, ARRAY[$5]::double precision[], $6)).count $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuecount(rastertable text, rastercolumn text, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, searchvalues double precision[] DEFAULT NULL::double precision[], roundto double precision DEFAULT 0, OUT value double precision, OUT count integer)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE
AS $function$ SELECT value, count FROM public._ST_valuecount($1, $2, $3, $4, $5, $6) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuecount(rast raster, searchvalue double precision, roundto double precision DEFAULT 0)
 RETURNS integer
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT ( public._ST_valuecount($1, 1, TRUE, ARRAY[$2]::double precision[], $3)).count $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuecount(rastertable text, rastercolumn text, nband integer, searchvalue double precision, roundto double precision DEFAULT 0)
 RETURNS integer
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT ( public._ST_valuecount($1, $2, $3, TRUE, ARRAY[$4]::double precision[], $5)).count $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuepercent(rastertable text, rastercolumn text, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, searchvalues double precision[] DEFAULT NULL::double precision[], roundto double precision DEFAULT 0, OUT value double precision, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE
AS $function$ SELECT value, percent FROM public._ST_valuecount($1, $2, $3, $4, $5, $6) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuepercent(rast raster, nband integer, searchvalue double precision, roundto double precision DEFAULT 0)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT ( public._ST_valuecount($1, $2, TRUE, ARRAY[$3]::double precision[], $4)).percent $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuepercent(rastertable text, rastercolumn text, nband integer, searchvalues double precision[], roundto double precision DEFAULT 0, OUT value double precision, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE
AS $function$ SELECT value, percent FROM public._ST_valuecount($1, $2, $3, TRUE, $4, $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuepercent(rastertable text, rastercolumn text, searchvalues double precision[], roundto double precision DEFAULT 0, OUT value double precision, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 STABLE
AS $function$ SELECT value, percent FROM public._ST_valuecount($1, $2, 1, TRUE, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuepercent(rastertable text, rastercolumn text, nband integer, exclude_nodata_value boolean, searchvalue double precision, roundto double precision DEFAULT 0)
 RETURNS double precision
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT ( public._ST_valuecount($1, $2, $3, $4, ARRAY[$5]::double precision[], $6)).percent $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuepercent(rastertable text, rastercolumn text, nband integer, searchvalue double precision, roundto double precision DEFAULT 0)
 RETURNS double precision
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT ( public._ST_valuecount($1, $2, $3, TRUE, ARRAY[$4]::double precision[], $5)).percent $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuepercent(rastertable text, rastercolumn text, searchvalue double precision, roundto double precision DEFAULT 0)
 RETURNS double precision
 LANGUAGE sql
 STABLE STRICT
AS $function$ SELECT ( public._ST_valuecount($1, $2, 1, TRUE, ARRAY[$3]::double precision[], $4)).percent $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuepercent(rast raster, searchvalue double precision, roundto double precision DEFAULT 0)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT ( public._ST_valuecount($1, 1, TRUE, ARRAY[$2]::double precision[], $3)).percent $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuepercent(rast raster, nband integer DEFAULT 1, exclude_nodata_value boolean DEFAULT true, searchvalues double precision[] DEFAULT NULL::double precision[], roundto double precision DEFAULT 0, OUT value double precision, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT value, percent FROM public._ST_valuecount($1, $2, $3, $4, $5) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuepercent(rast raster, nband integer, exclude_nodata_value boolean, searchvalue double precision, roundto double precision DEFAULT 0)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT ( public._ST_valuecount($1, $2, $3, ARRAY[$4]::double precision[], $5)).percent $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuepercent(rast raster, searchvalues double precision[], roundto double precision DEFAULT 0, OUT value double precision, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT value, percent FROM public._ST_valuecount($1, 1, TRUE, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_valuepercent(rast raster, nband integer, searchvalues double precision[], roundto double precision DEFAULT 0, OUT value double precision, OUT percent double precision)
 RETURNS SETOF record
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT value, percent FROM public._ST_valuecount($1, $2, TRUE, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_voronoilines(g1 geometry, tolerance double precision DEFAULT 0.0, extend_to geometry DEFAULT NULL::geometry)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE COST 1
AS $function$ SELECT public._ST_Voronoi(g1, extend_to, tolerance, false) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_voronoipolygons(g1 geometry, tolerance double precision DEFAULT 0.0, extend_to geometry DEFAULT NULL::geometry)
 RETURNS geometry
 LANGUAGE sql
 IMMUTABLE COST 1
AS $function$ SELECT public._ST_Voronoi(g1, extend_to, tolerance, true) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_width(raster)
 RETURNS integer
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/rtpostgis-2.5', $function$RASTER_getWidth$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_within(geom1 geometry, geom2 geometry)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE
AS $function$SELECT $2 OPERATOR(public.~) $1 AND public._ST_Contains($2,$1)$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_within(rast1 raster, nband1 integer, rast2 raster, nband2 integer)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT $1 OPERATOR(public.&&) $3 AND CASE WHEN $2 IS NULL OR $4 IS NULL THEN public._st_within(public.st_convexhull($1), public.st_convexhull($3)) ELSE public._st_contains($3, $4, $1, $2) END $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_within(rast1 raster, rast2 raster)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 1000
AS $function$ SELECT public.st_within($1, NULL::integer, $2, NULL::integer) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_wkbtosql(wkb bytea)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_from_WKB$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_wkttosql(text)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_from_text$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_worldtorastercoord(rast raster, longitude double precision, latitude double precision, OUT columnx integer, OUT rowy integer)
 RETURNS record
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT columnx, rowy FROM public._ST_worldtorastercoord($1, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_worldtorastercoord(rast raster, pt geometry, OUT columnx integer, OUT rowy integer)
 RETURNS record
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
	DECLARE
		rx integer;
		ry integer;
	BEGIN
		IF public.ST_geometrytype(pt) != 'ST_Point' THEN
			RAISE EXCEPTION 'Attempting to compute raster coordinate with a non-point geometry';
		END IF;
		IF public.ST_SRID(rast) != public.ST_SRID(pt) THEN
			RAISE EXCEPTION 'Raster and geometry do not have the same SRID';
		END IF;

		SELECT rc.columnx AS x, rc.rowy AS y INTO columnx, rowy FROM public._ST_worldtorastercoord($1, public.ST_x(pt), public.ST_y(pt)) AS rc;
		RETURN;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_worldtorastercoordx(rast raster, xw double precision)
 RETURNS integer
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT columnx FROM public._ST_worldtorastercoord($1, $2, NULL) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_worldtorastercoordx(rast raster, xw double precision, yw double precision)
 RETURNS integer
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT columnx FROM public._ST_worldtorastercoord($1, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_worldtorastercoordx(rast raster, pt geometry)
 RETURNS integer
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
	DECLARE
		xr integer;
	BEGIN
		IF ( public.ST_geometrytype(pt) != 'ST_Point' ) THEN
			RAISE EXCEPTION 'Attempting to compute raster coordinate with a non-point geometry';
		END IF;
		IF public.ST_SRID(rast) != public.ST_SRID(pt) THEN
			RAISE EXCEPTION 'Raster and geometry do not have the same SRID';
		END IF;
		SELECT columnx INTO xr FROM public._ST_worldtorastercoord($1, public.ST_x(pt), public.ST_y(pt));
		RETURN xr;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_worldtorastercoordy(rast raster, pt geometry)
 RETURNS integer
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
	DECLARE
		yr integer;
	BEGIN
		IF ( st_geometrytype(pt) != 'ST_Point' ) THEN
			RAISE EXCEPTION 'Attempting to compute raster coordinate with a non-point geometry';
		END IF;
		IF ST_SRID(rast) != ST_SRID(pt) THEN
			RAISE EXCEPTION 'Raster and geometry do not have the same SRID';
		END IF;
		SELECT rowy INTO yr FROM public._ST_worldtorastercoord($1, st_x(pt), st_y(pt));
		RETURN yr;
	END;
	$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_worldtorastercoordy(rast raster, xw double precision, yw double precision)
 RETURNS integer
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT rowy FROM public._ST_worldtorastercoord($1, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_worldtorastercoordy(rast raster, yw double precision)
 RETURNS integer
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$ SELECT rowy FROM public._ST_worldtorastercoord($1, NULL, $2) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_wrapx(geom geometry, wrap double precision, move double precision)
 RETURNS geometry
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$ST_WrapX$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_x(geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_x_point$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_xmax(box3d)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX3D_xmax$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_xmin(box3d)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX3D_xmin$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_y(geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_y_point$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_ymax(box3d)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX3D_ymax$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_ymin(box3d)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX3D_ymin$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_z(geometry)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$LWGEOM_z_point$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_zmax(box3d)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX3D_zmax$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_zmflag(geometry)
 RETURNS smallint
 LANGUAGE c
 IMMUTABLE STRICT COST 5
AS '$libdir/postgis-2.5', $function$LWGEOM_zmflag$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.st_zmin(box3d)
 RETURNS double precision
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/postgis-2.5', $function$BOX3D_zmin$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.text(geometry)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT COST 25
AS '$libdir/postgis-2.5', $function$LWGEOM_to_text$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.text_soundex(text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE STRICT
AS '$libdir/fuzzystrmatch', $function$soundex$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.unifyspacechars(chaostext text)
 RETURNS text
 LANGUAGE plpgsql
AS $function$

	BEGIN
		RETURN regexp_replace(chaostext, '[\s\u180e\u200B\u200C\u200D\u2060\uFEFF\u00a0]',' ','g'); 
	END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.unifyspacesandtrim(chaostext text)
 RETURNS text
 LANGUAGE plpgsql
AS $function$

	BEGIN
		-- start by replacing all spaces of any kind with latin 0020
		-- then trim that normal space from the end and beginning
		RETURN regexp_replace(
					regexp_replace(
						regexp_replace(chaostext, '[\s\u180e\u200B\u200C\u200D\u2060\uFEFF\u00a0]',' ','g'), 
						'\s+$',''),
						'^\s+','');
	END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.unlockrows(text)
 RETURNS integer
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
	ret int;
BEGIN

	IF NOT LongTransactionsEnabled() THEN
		RAISE EXCEPTION 'Long transaction support disabled, use EnableLongTransaction() to enable.';
	END IF;

	EXECUTE 'DELETE FROM authorization_table where authid = ' ||
		quote_literal($1);

	GET DIAGNOSTICS ret = ROW_COUNT;

	RETURN ret;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.updategeometrysrid(catalogn_name character varying, schema_name character varying, table_name character varying, column_name character varying, new_srid_in integer)
 RETURNS text
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
	myrec RECORD;
	okay boolean;
	cname varchar;
	real_schema name;
	unknown_srid integer;
	new_srid integer := new_srid_in;

BEGIN

	-- Find, check or fix schema_name
	IF ( schema_name != '' ) THEN
		okay = false;

		FOR myrec IN SELECT nspname FROM pg_namespace WHERE text(nspname) = schema_name LOOP
			okay := true;
		END LOOP;

		IF ( okay <> true ) THEN
			RAISE EXCEPTION 'Invalid schema name';
		ELSE
			real_schema = schema_name;
		END IF;
	ELSE
		SELECT INTO real_schema current_schema()::text;
	END IF;

	-- Ensure that column_name is in geometry_columns
	okay = false;
	FOR myrec IN SELECT type, coord_dimension FROM public.geometry_columns WHERE f_table_schema = text(real_schema) and f_table_name = table_name and f_geometry_column = column_name LOOP
		okay := true;
	END LOOP;
	IF (NOT okay) THEN
		RAISE EXCEPTION 'column not found in geometry_columns table';
		RETURN false;
	END IF;

	-- Ensure that new_srid is valid
	IF ( new_srid > 0 ) THEN
		IF ( SELECT count(*) = 0 from spatial_ref_sys where srid = new_srid ) THEN
			RAISE EXCEPTION 'invalid SRID: % not found in spatial_ref_sys', new_srid;
			RETURN false;
		END IF;
	ELSE
		unknown_srid := public.ST_SRID('POINT EMPTY'::public.geometry);
		IF ( new_srid != unknown_srid ) THEN
			new_srid := unknown_srid;
			RAISE NOTICE 'SRID value % converted to the officially unknown SRID value %', new_srid_in, new_srid;
		END IF;
	END IF;

	IF postgis_constraint_srid(real_schema, table_name, column_name) IS NOT NULL THEN
	-- srid was enforced with constraints before, keep it that way.
        -- Make up constraint name
        cname = 'enforce_srid_'  || column_name;

        -- Drop enforce_srid constraint
        EXECUTE 'ALTER TABLE ' || quote_ident(real_schema) ||
            '.' || quote_ident(table_name) ||
            ' DROP constraint ' || quote_ident(cname);

        -- Update geometries SRID
        EXECUTE 'UPDATE ' || quote_ident(real_schema) ||
            '.' || quote_ident(table_name) ||
            ' SET ' || quote_ident(column_name) ||
            ' = public.ST_SetSRID(' || quote_ident(column_name) ||
            ', ' || new_srid::text || ')';

        -- Reset enforce_srid constraint
        EXECUTE 'ALTER TABLE ' || quote_ident(real_schema) ||
            '.' || quote_ident(table_name) ||
            ' ADD constraint ' || quote_ident(cname) ||
            ' CHECK (st_srid(' || quote_ident(column_name) ||
            ') = ' || new_srid::text || ')';
    ELSE
        -- We will use typmod to enforce if no srid constraints
        -- We are using postgis_type_name to lookup the new name
        -- (in case Paul changes his mind and flips geometry_columns to return old upper case name)
        EXECUTE 'ALTER TABLE ' || quote_ident(real_schema) || '.' || quote_ident(table_name) ||
        ' ALTER COLUMN ' || quote_ident(column_name) || ' TYPE  geometry(' || public.postgis_type_name(myrec.type, myrec.coord_dimension, true) || ', ' || new_srid::text || ') USING public.ST_SetSRID(' || quote_ident(column_name) || ',' || new_srid::text || ');' ;
    END IF;

	RETURN real_schema || '.' || table_name || '.' || column_name ||' SRID changed to ' || new_srid::text;

END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.updategeometrysrid(character varying, character varying, character varying, integer)
 RETURNS text
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
	ret  text;
BEGIN
	SELECT public.UpdateGeometrySRID('',$1,$2,$3,$4) into ret;
	RETURN ret;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.updategeometrysrid(character varying, character varying, integer)
 RETURNS text
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
	ret  text;
BEGIN
	SELECT public.UpdateGeometrySRID('','',$1,$2,$3) into ret;
	RETURN ret;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.updaterastersrid(table_name name, column_name name, new_srid integer)
 RETURNS boolean
 LANGUAGE sql
 STRICT
AS $function$ SELECT  public._UpdateRasterSRID('', $1, $2, $3) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION public.updaterastersrid(schema_name name, table_name name, column_name name, new_srid integer)
 RETURNS boolean
 LANGUAGE sql
 STRICT
AS $function$ SELECT  public._UpdateRasterSRID($1, $2, $3, $4) $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.count_words(character varying)
 RETURNS integer
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
DECLARE
  tempString VARCHAR;
  tempInt INTEGER;
  count INTEGER := 1;
  lastSpace BOOLEAN := FALSE;
BEGIN
  IF $1 IS NULL THEN
    return -1;
  END IF;
  tempInt := length($1);
  IF tempInt = 0 THEN
    return 0;
  END IF;
  FOR i IN 1..tempInt LOOP
    tempString := substring($1 from i for 1);
    IF tempString = ' ' THEN
      IF NOT lastSpace THEN
        count := count + 1;
      END IF;
      lastSpace := TRUE;
    ELSE
      lastSpace := FALSE;
    END IF;
  END LOOP;
  return count;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.create_census_base_tables()
 RETURNS text
 LANGUAGE plpgsql
AS $function$
DECLARE var_temp text;
BEGIN
var_temp := tiger.SetSearchPathForInstall('tiger');
IF NOT EXISTS(SELECT table_name FROM information_schema.columns WHERE table_schema = 'tiger' AND column_name = 'tract_id' AND table_name = 'tract')  THEN
	-- census block group/tracts parent tables not created yet or an older version -- drop old if not in use, create new structure
	DROP TABLE IF EXISTS tiger.tract;
	CREATE TABLE tract
	(
	  gid serial NOT NULL,
	  statefp varchar(2),
	  countyfp varchar(3),
	  tractce varchar(6),
	  tract_id varchar(11) PRIMARY KEY,
	  name varchar(7),
	  namelsad varchar(20),
	  mtfcc varchar(5),
	  funcstat varchar(1),
	  aland double precision,
	  awater double precision,
	  intptlat varchar(11),
	  intptlon varchar(12),
	  the_geom geometry,
	  CONSTRAINT enforce_dims_geom CHECK (st_ndims(the_geom) = 2),
	  CONSTRAINT enforce_geotype_geom CHECK (geometrytype(the_geom) = 'MULTIPOLYGON'::text OR the_geom IS NULL),
	  CONSTRAINT enforce_srid_geom CHECK (st_srid(the_geom) = 4269)
	);

	DROP TABLE IF EXISTS tiger.tabblock;
	CREATE TABLE tabblock
	(
	  gid serial NOT NULL,
	  statefp varchar(2),
	  countyfp varchar(3),
	  tractce varchar(6),
	  blockce varchar(4),
	  tabblock_id varchar(16) PRIMARY KEY,
	  name varchar(20),
	  mtfcc varchar(5),
	  ur varchar(1),
	  uace varchar(5),
	  funcstat varchar(1),
	  aland double precision,
	  awater double precision,
	  intptlat varchar(11),
	  intptlon varchar(12),
	  the_geom geometry,
	  CONSTRAINT enforce_dims_geom CHECK (st_ndims(the_geom) = 2),
	  CONSTRAINT enforce_geotype_geom CHECK (geometrytype(the_geom) = 'MULTIPOLYGON'::text OR the_geom IS NULL),
	  CONSTRAINT enforce_srid_geom CHECK (st_srid(the_geom) = 4269)
	);

	DROP TABLE IF EXISTS tiger.bg;
	CREATE TABLE bg
	(
	  gid serial NOT NULL,
	  statefp varchar(2),
	  countyfp varchar(3),
	  tractce varchar(6),
	  blkgrpce varchar(1),
	  bg_id varchar(12) PRIMARY KEY,
	  namelsad varchar(13),
	  mtfcc varchar(5),
	  funcstat varchar(1),
	  aland double precision,
	  awater double precision,
	  intptlat varchar(11),
	  intptlon varchar(12),
	  the_geom geometry,
	  CONSTRAINT enforce_dims_geom CHECK (st_ndims(the_geom) = 2),
	  CONSTRAINT enforce_geotype_geom CHECK (geometrytype(the_geom) = 'MULTIPOLYGON'::text OR the_geom IS NULL),
	  CONSTRAINT enforce_srid_geom CHECK (st_srid(the_geom) = 4269)
	);
	COMMENT ON TABLE tiger.bg IS 'block groups';
END IF;

IF EXISTS(SELECT * FROM information_schema.columns WHERE table_schema = 'tiger' AND column_name = 'tabblock_id' AND table_name = 'tabblock' AND character_maximum_length < 16)  THEN -- size of name and tabblock_id fields need to be increased
    ALTER TABLE tiger.tabblock ALTER COLUMN name TYPE varchar(20);
    ALTER TABLE tiger.tabblock ALTER COLUMN tabblock_id TYPE varchar(16);
    RAISE NOTICE 'Size of tabblock_id and name are being increased';
END IF;
RETURN 'Tables already present';
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.cull_null(character varying)
 RETURNS character varying
 LANGUAGE sql
 IMMUTABLE
AS $function$
    SELECT coalesce($1,'');
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.diff_zip(zip1 character varying, zip2 character varying)
 RETURNS integer
 LANGUAGE sql
 IMMUTABLE STRICT COST 200
AS $function$ SELECT abs(to_number( CASE WHEN trim(substring($1,1,5)) ~ '^[0-9]+$' THEN $1 ELSE '0' END,'99999')::integer - to_number( CASE WHEN trim(substring($2,1,5)) ~ '^[0-9]+$' THEN $2 ELSE '0' END,'99999')::integer )::integer;  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.drop_dupe_featnames_generate_script()
 RETURNS text
 LANGUAGE sql
AS $function$

SELECT array_to_string(ARRAY(SELECT 'CREATE TEMPORARY TABLE dup AS
SELECT min(f.gid) As min_gid, f.tlid, lower(f.fullname) As fname
	FROM ONLY ' || t.table_schema || '.' || t.table_name || ' As f
	GROUP BY f.tlid, lower(f.fullname)
	HAVING count(*) > 1;

DELETE FROM ' || t.table_schema || '.' || t.table_name || ' AS feat
WHERE EXISTS (SELECT tlid FROM dup WHERE feat.tlid = dup.tlid AND lower(feat.fullname) = dup.fname
		AND feat.gid > dup.min_gid);
DROP TABLE dup;
CREATE INDEX idx_' || t.table_schema || '_' || t.table_name || '_tlid ' || ' ON ' || t.table_schema || '.' || t.table_name || ' USING btree(tlid);
' As drop_sql_create_index
FROM (SELECT table_name, table_schema FROM
	information_schema.tables WHERE table_type = 'BASE TABLE' AND (table_name LIKE '%featnames' ) AND table_schema IN('tiger','tiger_data')) As t
		LEFT JOIN pg_catalog.pg_indexes i ON
			(i.tablename = t.table_name AND i.schemaname = t.table_schema
				AND  indexdef LIKE '%btree%(%tlid%')
WHERE i.tablename IS NULL) ,E'\r');

$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.drop_indexes_generate_script(tiger_data_schema text DEFAULT 'tiger_data'::text)
 RETURNS text
 LANGUAGE sql
 STABLE
AS $function$
SELECT array_to_string(ARRAY(SELECT 'DROP INDEX ' || schemaname || '.' || indexname || ';'
FROM pg_catalog.pg_indexes  where schemaname IN('tiger',$1)  AND indexname NOT LIKE 'uidx%' AND indexname NOT LIKE 'pk_%' AND indexname NOT LIKE '%key'), E'\n');
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.drop_nation_tables_generate_script(param_schema text DEFAULT 'tiger_data'::text)
 RETURNS text
 LANGUAGE sql
AS $function$
SELECT array_to_string(array_agg('DROP TABLE ' || quote_ident(table_schema) || '.' || quote_ident(table_name) || ';'),E'\n')
	FROM (SELECT * FROM information_schema.tables
	WHERE table_schema = $1 AND (table_name ~ E'^[a-z]{2}\_county' or table_name ~ E'^[a-z]{2}\_state' or table_name = 'state_all' or table_name LIKE 'county_all%' or table_name LIKE 'zcta5_all%') ORDER BY table_name) AS foo;
;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.drop_state_tables_generate_script(param_state text, param_schema text DEFAULT 'tiger_data'::text)
 RETURNS text
 LANGUAGE sql
AS $function$
SELECT array_to_string(array_agg('DROP TABLE ' || quote_ident(table_schema) || '.' || quote_ident(table_name) || ';'),E'\n')
	FROM (SELECT * FROM information_schema.tables
	WHERE table_schema = $2 AND table_name like lower($1) || '_%' ORDER BY table_name) AS foo;
;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.end_soundex(character varying)
 RETURNS character varying
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
DECLARE
  tempString VARCHAR;
BEGIN
  tempString := substring($1, E'[ ,.\n\t\f]([a-zA-Z0-9]*)$');
  IF tempString IS NOT NULL THEN
    tempString := soundex(tempString);
  ELSE
    tempString := soundex($1);
  END IF;
  return tempString;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.geocode(in_addy norm_addy, max_results integer DEFAULT 10, restrict_geom geometry DEFAULT NULL::geometry, OUT addy norm_addy, OUT geomout geometry, OUT rating integer)
 RETURNS SETOF record
 LANGUAGE plpgsql
 STABLE COST 1000
AS $function$
DECLARE
  rec RECORD;
BEGIN

  IF NOT IN_ADDY.parsed THEN
    RETURN;
  END IF;

  -- Go for the full monty if we've got enough info
  IF IN_ADDY.streetName IS NOT NULL AND
      (IN_ADDY.zip IS NOT NULL OR IN_ADDY.stateAbbrev IS NOT NULL) THEN

    FOR rec IN
        SELECT *
        FROM
          (SELECT
            DISTINCT ON (
              (a.addy).address,
              (a.addy).predirabbrev,
              (a.addy).streetname,
              (a.addy).streettypeabbrev,
              (a.addy).postdirabbrev,
              (a.addy).internal,
              (a.addy).location,
              (a.addy).stateabbrev,
              (a.addy).zip
              )
            *
           FROM
             tiger.geocode_address(IN_ADDY, max_results, restrict_geom) a
           ORDER BY
              (a.addy).address,
              (a.addy).predirabbrev,
              (a.addy).streetname,
              (a.addy).streettypeabbrev,
              (a.addy).postdirabbrev,
              (a.addy).internal,
              (a.addy).location,
              (a.addy).stateabbrev,
              (a.addy).zip,
              a.rating
          ) as b
        ORDER BY b.rating LIMIT max_results
    LOOP

      ADDY := rec.addy;
      GEOMOUT := rec.geomout;
      RATING := rec.rating;

      RETURN NEXT;

      IF RATING = 0 THEN
        RETURN;
      END IF;

    END LOOP;

    IF RATING IS NOT NULL THEN
      RETURN;
    END IF;
  END IF;

  -- No zip code, try state/location, need both or we'll get too much stuffs.
  IF IN_ADDY.zip IS NOT NULL OR (IN_ADDY.stateAbbrev IS NOT NULL AND IN_ADDY.location IS NOT NULL) THEN
    FOR rec in SELECT * FROM tiger.geocode_location(IN_ADDY, restrict_geom) As b ORDER BY b.rating LIMIT max_results
    LOOP
      ADDY := rec.addy;
      GEOMOUT := rec.geomout;
      RATING := rec.rating;

      RETURN NEXT;
      IF RATING = 100 THEN
        RETURN;
      END IF;
    END LOOP;

  END IF;

  RETURN;

END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.geocode(input character varying, max_results integer DEFAULT 10, restrict_geom geometry DEFAULT NULL::geometry, OUT addy norm_addy, OUT geomout geometry, OUT rating integer)
 RETURNS SETOF record
 LANGUAGE plpgsql
 STABLE
AS $function$
DECLARE
  rec RECORD;
BEGIN

  IF input IS NULL THEN
    RETURN;
  END IF;

  -- Pass the input string into the address normalizer
  ADDY := normalize_address(input);
  IF NOT ADDY.parsed THEN
    RETURN;
  END IF;

/*  FOR rec IN SELECT * FROM geocode(ADDY)
  LOOP

    ADDY := rec.addy;
    GEOMOUT := rec.geomout;
    RATING := rec.rating;

    RETURN NEXT;
  END LOOP;*/

  RETURN QUERY SELECT g.addy, g.geomout, g.rating FROM geocode(ADDY, max_results, restrict_geom) As g ORDER BY g.rating;

END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.geocode_address(parsed norm_addy, max_results integer DEFAULT 10, restrict_geom geometry DEFAULT NULL::geometry, OUT addy norm_addy, OUT geomout geometry, OUT rating integer)
 RETURNS SETOF record
 LANGUAGE plpgsql
 STABLE COST 1000 ROWS 50
 SET join_collapse_limit TO '2'
AS $function$
DECLARE
  results RECORD;
  zip_info RECORD;
  stmt VARCHAR;
  in_statefp VARCHAR;
  exact_street boolean := false;
  var_debug boolean := get_geocode_setting('debug_geocode_address')::boolean;
  var_sql text := '';
  var_n integer := 0;
  var_restrict_geom geometry := NULL;
  var_bfilter text := null;
  var_bestrating integer := NULL;
  var_zip_penalty numeric := get_geocode_setting('zip_penalty')::numeric*1.00;
BEGIN
  IF parsed.streetName IS NULL THEN
    -- A street name must be given.  Think about it.
    RETURN;
  END IF;

  ADDY.internal := parsed.internal;

  IF parsed.stateAbbrev IS NOT NULL THEN
    in_statefp := statefp FROM state_lookup As s WHERE s.abbrev = parsed.stateAbbrev;
  END IF;

  IF in_statefp IS NULL THEN
  --if state is not provided or was bogus, just pick the first where the zip is present
    in_statefp := statefp FROM zip_lookup_base WHERE zip = substring(parsed.zip,1,5) LIMIT 1;
  END IF;

  IF restrict_geom IS NOT NULL THEN
  		IF ST_SRID(restrict_geom) < 1 OR ST_SRID(restrict_geom) = 4236 THEN
  		-- basically has no srid or if wgs84 close enough to NAD 83 -- assume same as data
  			var_restrict_geom = ST_SetSRID(restrict_geom,4269);
  		ELSE
  		--transform and snap
  			var_restrict_geom = ST_SnapToGrid(ST_Transform(restrict_geom, 4269), 0.000001);
  		END IF;
  END IF;
  var_bfilter := ' SELECT zcta5ce FROM tiger.zcta5 AS zc
                    WHERE zc.statefp = ' || quote_nullable(in_statefp) || '
                        AND ST_Intersects(zc.the_geom, ' || quote_literal(var_restrict_geom::text) || '::geometry)  ' ;

  SELECT NULL::varchar[] As zip INTO zip_info;

  IF parsed.zip IS NOT NULL  THEN
  -- Create an array of 5 zips containing 2 before and 2 after our target if our streetName is longer
    IF length(parsed.streetName) > 7 THEN
        SELECT zip_range(parsed.zip, -2, 2) As zip INTO zip_info;
    ELSE
    -- If our street name is short, we'll run into many false positives so reduce our zip window a bit
        SELECT zip_range(parsed.zip, -1, 1) As zip INTO zip_info;
    END IF;
    --This signals bad zip input, only use the range if it falls in the place zip range
    IF length(parsed.zip) != 5 AND parsed.location IS NOT NULL THEN
         stmt := 'SELECT ARRAY(SELECT DISTINCT zip
          FROM tiger.zip_lookup_base AS z
         WHERE z.statefp = $1
               AND  z.zip = ANY($3) AND lower(z.city) LIKE lower($2) || ''%''::text '  || COALESCE(' AND z.zip IN(' || var_bfilter || ')', '') || ')::varchar[] AS zip ORDER BY zip' ;
         EXECUTE stmt INTO zip_info USING in_statefp, parsed.location, zip_info.zip;
         IF var_debug THEN
            RAISE NOTICE 'Bad zip newzip range: %', quote_nullable(zip_info.zip);
         END IF;
        IF array_upper(zip_info.zip,1) = 0 OR array_upper(zip_info.zip,1) IS NULL THEN
        -- zips do not fall in city ignore them
            IF var_debug THEN
                RAISE NOTICE 'Ignore new zip range that is bad too: %', quote_nullable(zip_info.zip);
            END IF;
            zip_info.zip = NULL::varchar[];
        END IF;
    END IF;
  END IF;
  IF zip_info.zip IS NULL THEN
  -- If no good zips just include all for the location
  -- We do a like instead of absolute check since tiger sometimes tacks things like Town at end of places
    stmt := 'SELECT ARRAY(SELECT DISTINCT zip
          FROM tiger.zip_lookup_base AS z
         WHERE z.statefp = $1
               AND  lower(z.city) LIKE lower($2) || ''%''::text '  || COALESCE(' AND z.zip IN(' || var_bfilter || ')', '') || ')::varchar[] AS zip ORDER BY zip' ;
    EXECUTE stmt INTO zip_info USING in_statefp, parsed.location;
    IF var_debug THEN
        RAISE NOTICE 'Zip range based on only considering city: %', quote_nullable(zip_info.zip);
    END IF;
  END IF;
   -- Brute force -- try to find perfect matches and exit if we have one
   -- we first pull all the names in zip and rank by if zip matches input zip and streetname matches street
  stmt := 'WITH a AS
  	( SELECT *
  		FROM (SELECT f.*, ad.side, ad.zip, ad.fromhn, ad.tohn,
  					RANK() OVER(ORDER BY ' || CASE WHEN parsed.zip > '' THEN ' diff_zip(ad.zip,$7)*$11 + ' ELSE '' END
						||' CASE WHEN lower(f.name) = lower($2) THEN 0 ELSE levenshtein_ignore_case(f.name, lower($2) )  END +
						levenshtein_ignore_case(f.fullname, lower($2 || '' '' || COALESCE($4,'''')) )
						+ CASE WHEN (greatest_hn(ad.fromhn,ad.tohn) % 2)::integer = ($1 % 2)::integer THEN 0 ELSE 1 END
						+ CASE WHEN $1::integer BETWEEN least_hn(ad.fromhn,ad.tohn) AND greatest_hn(ad.fromhn, ad.tohn)
							THEN 0 ELSE 4 END
							+ CASE WHEN lower($4) = lower(f.suftypabrv) OR lower($4) = lower(f.pretypabrv) THEN 0 ELSE 1 END
							+ rate_attributes($5, f.predirabrv,'
         || '    $2,  f.name , $4,'
         || '    suftypabrv , $6,'
         || '    sufdirabrv, prequalabr)
							)
						As rank
                		FROM tiger.featnames As f INNER JOIN tiger.addr As ad ON (f.tlid = ad.tlid)
                    WHERE $10 = f.statefp AND $10 = ad.statefp
                    	'
                    || CASE WHEN length(parsed.streetName) > 5  THEN ' AND (lower(f.fullname) LIKE (COALESCE($5 || '' '','''') || lower($2) || ''%'')::text OR lower(f.name) = lower($2) OR soundex(f.name) = soundex($2) ) ' ELSE  ' AND lower(f.name) = lower($2) ' END
                    || CASE WHEN zip_info.zip IS NOT NULL THEN '    AND ( ad.zip = ANY($9::varchar[]) )  ' ELSE '' END
            || ' ) AS foo ORDER BY rank LIMIT ' || max_results*3 || ' )
  	SELECT * FROM (
    SELECT DISTINCT ON (sub.predirabrv,sub.fename,COALESCE(sub.suftypabrv, sub.pretypabrv) ,sub.sufdirabrv,sub.place,s.stusps,sub.zip)'
         || '    sub.predirabrv   as fedirp,'
         || '    sub.fename,'
         || '    COALESCE(sub.suftypabrv, sub.pretypabrv)   as fetype,'
         || '    sub.sufdirabrv   as fedirs,'
         || '    sub.place ,'
         || '    s.stusps as state,'
         || '    sub.zip as zip,'
         || '    interpolate_from_address($1, sub.fromhn,'
         || '        sub.tohn, sub.the_geom, sub.side) as address_geom,'
         || '       (sub.sub_rating + '
         || CASE WHEN parsed.zip > '' THEN '  least(coalesce(diff_zip($7 , sub.zip),0), 20)*$11  '
            ELSE '1' END::text
         || ' + coalesce(levenshtein_ignore_case($3, sub.place),5) )::integer'
         || '    as sub_rating,'
         || '    sub.exact_address as exact_address, sub.tohn, sub.fromhn '
         || ' FROM ('
         || '  SELECT tlid, predirabrv, COALESCE(b.prequalabr || '' '','''' ) || b.name As fename, suftypabrv, sufdirabrv, fromhn, tohn,
                    side,  zip, rate_attributes($5, predirabrv,'
         || '    $2,  b.name , $4,'
         || '    suftypabrv , $6,'
         || '    sufdirabrv, prequalabr) + '
         || '    CASE '
         || '        WHEN $1::integer IS NULL OR b.fromhn IS NULL THEN 20'
         || '        WHEN $1::integer >= least_hn(b.fromhn, b.tohn) '
         || '            AND $1::integer <= greatest_hn(b.fromhn,b.tohn)'
         || '            AND ($1::integer % 2) = (to_number(b.fromhn,''99999999'') % 2)::integer'
         || '            THEN 0'
         || '        WHEN $1::integer >= least_hn(b.fromhn,b.tohn)'
         || '            AND $1::integer <= greatest_hn(b.fromhn,b.tohn)'
         || '            THEN 2'
         || '        ELSE'
         || '            ((1.0 - '
         ||              '(least_hn($1::text,least_hn(b.fromhn,b.tohn)::text)::numeric /'
         ||              ' (greatest(1,greatest_hn($1::text,greatest_hn(b.fromhn,b.tohn)::text))) )'
         ||              ') * 5)::integer + 5'
         || '        END::integer'
         || '    AS sub_rating,$1::integer >= least_hn(b.fromhn,b.tohn) '
         || '            AND $1::integer <= greatest_hn(b.fromhn,b.tohn) '
         || '            AND ($1 % 2)::numeric::integer = (to_number(b.fromhn,''99999999'') % 2)'
         || '    as exact_address, b.name, b.prequalabr, b.pretypabrv, b.tfidr, b.tfidl, b.the_geom, b.place '
         || '  FROM
             (SELECT   a.tlid, a.fullname, a.name, a.predirabrv, a.suftypabrv, a.sufdirabrv, a.prequalabr, a.pretypabrv,
                b.the_geom, tfidr, tfidl,
                a.side ,
                a.fromhn,
                a.tohn,
                a.zip,
                p.name as place

                FROM  a INNER JOIN tiger.edges As b ON (a.statefp = b.statefp AND a.tlid = b.tlid  '
               || ')
                    INNER JOIN tiger.faces AS f ON ($10 = f.statefp AND ( (b.tfidl = f.tfid AND a.side = ''L'') OR (b.tfidr = f.tfid AND a.side = ''R'' ) ))
                    INNER JOIN tiger.place p ON ($10 = p.statefp AND f.placefp = p.placefp '
          || CASE WHEN parsed.location > '' AND zip_info.zip IS NULL THEN ' AND ( lower(p.name) LIKE (lower($3::text) || ''%'')  ) ' ELSE '' END
          || ')
                WHERE a.statefp = $10  AND  b.statefp = $10   '
             ||   CASE WHEN var_restrict_geom IS NOT NULL THEN ' AND ST_Intersects(b.the_geom, $8::geometry) '  ELSE '' END
             || '

          )   As b
           ORDER BY 10 ,  11 DESC
           LIMIT 20
            ) AS sub
          JOIN tiger.state s ON ($10 = s.statefp)
            ORDER BY 1,2,3,4,5,6,7,9
          LIMIT 20) As foo ORDER BY sub_rating, exact_address DESC LIMIT  ' || max_results*10 ;

  IF var_debug THEN
         RAISE NOTICE 'stmt: %',
            replace( replace( replace(
                replace(
                replace(replace( replace(replace(replace(replace( replace(stmt,'$11', var_zip_penalty::text), '$10', quote_nullable(in_statefp) ), '$2',quote_nullable(parsed.streetName)),'$3',
                quote_nullable(parsed.location)), '$4', quote_nullable(parsed.streetTypeAbbrev) ),
                '$5', quote_nullable(parsed.preDirAbbrev) ),
                   '$6', quote_nullable(parsed.postDirAbbrev) ),
                   '$7', quote_nullable(parsed.zip) ),
                   '$8', quote_nullable(var_restrict_geom::text) ),
                   '$9', quote_nullable(zip_info.zip) ), '$1', quote_nullable(parsed.address) );
        --RAISE NOTICE 'PREPARE query_base_geo(integer, varchar,varchar,varchar,varchar,varchar,varchar,geometry,varchar[]) As %', stmt;
        --RAISE NOTICE 'EXECUTE query_base_geo(%,%,%,%,%,%,%,%,%); ', parsed.address,quote_nullable(parsed.streetName), quote_nullable(parsed.location), quote_nullable(parsed.streetTypeAbbrev), quote_nullable(parsed.preDirAbbrev), quote_nullable(parsed.postDirAbbrev), quote_nullable(parsed.zip), quote_nullable(var_restrict_geom::text), quote_nullable(zip_info.zip);
        --RAISE NOTICE 'DEALLOCATE query_base_geo;';
    END IF;
    FOR results IN EXECUTE stmt USING parsed.address,parsed.streetName, parsed.location, parsed.streetTypeAbbrev, parsed.preDirAbbrev, parsed.postDirAbbrev, parsed.zip, var_restrict_geom, zip_info.zip, in_statefp, var_zip_penalty LOOP

        -- If we found a match with an exact street, then don't bother
        -- trying to do non-exact matches

        exact_street := true;

        IF results.exact_address THEN
            ADDY.address := parsed.address;
        ELSE
            ADDY.address := CASE WHEN parsed.address > to_number(results.tohn,'99999999') AND parsed.address > to_number(results.fromhn, '99999999') THEN greatest_hn(results.fromhn, results.tohn)::integer
                ELSE least_hn(results.fromhn, results.tohn)::integer END ;
        END IF;

        ADDY.preDirAbbrev     := results.fedirp;
        ADDY.streetName       := results.fename;
        ADDY.streetTypeAbbrev := results.fetype;
        ADDY.postDirAbbrev    := results.fedirs;
        ADDY.location         := results.place;
        ADDY.stateAbbrev      := results.state;
        ADDY.zip              := results.zip;
        ADDY.parsed := TRUE;

        GEOMOUT := results.address_geom;
        RATING := results.sub_rating::integer;
        var_n := var_n + 1;

        IF var_bestrating IS NULL THEN
            var_bestrating := RATING; /** the first record to come is our best rating we will ever get **/
        END IF;

        -- Only consider matches with decent ratings
        IF RATING < 90 THEN
            RETURN NEXT;
        END IF;

        -- If we get an exact match, then just return that
        IF RATING = 0 THEN
            RETURN;
        END IF;

        IF var_n >= max_results AND RATING < 10  THEN --we have exceeded our desired limit and rating is not horrible
            RETURN;
        END IF;

    END LOOP;

    IF var_bestrating < 30 THEN --if we already have a match with a rating of 30 or less, its unlikely we can do any better
        RETURN;
    END IF;

-- There are a couple of different things to try, from the highest preference and falling back
  -- to lower-preference options.
  -- We start out with zip-code matching, where the zip code could possibly be in more than one
  -- state.  We loop through each state its in.
  -- Next, we try to find the location in our side-table, which is based off of the 'place' data exact first then sounds like
  -- Next, we look up the location/city and use the zip code which is returned from that
  -- Finally, if we didn't get a zip code or a city match, we fall back to just a location/street
  -- lookup to try and find *something* useful.
  -- In the end, we *have* to find a statefp, one way or another.
  var_sql :=
  ' SELECT statefp,location,a.zip,exact,min(pref) FROM
    (SELECT zip_state.statefp as statefp,$1 as location, true As exact, ARRAY[zip_state.zip] as zip,1 as pref
        FROM zip_state WHERE zip_state.zip = $2
            AND (' || quote_nullable(in_statefp) || ' IS NULL OR zip_state.statefp = ' || quote_nullable(in_statefp) || ')
          ' || COALESCE(' AND zip_state.zip IN(' || var_bfilter || ')', '') ||
        ' UNION SELECT zip_state_loc.statefp,zip_state_loc.place As location,false As exact, array_agg(zip_state_loc.zip) AS zip,1 + abs(COALESCE(diff_zip(max(zip), $2),0) - COALESCE(diff_zip(min(zip), $2),0))*$3 As pref
              FROM zip_state_loc
             WHERE zip_state_loc.statefp = ' || quote_nullable(in_statefp) || '
                   AND lower($1) = lower(zip_state_loc.place) '  || COALESCE(' AND zip_state_loc.zip IN(' || var_bfilter || ')', '') ||
        '     GROUP BY zip_state_loc.statefp,zip_state_loc.place
      UNION SELECT zip_state_loc.statefp,zip_state_loc.place As location,false As exact, array_agg(zip_state_loc.zip),3
              FROM zip_state_loc
             WHERE zip_state_loc.statefp = ' || quote_nullable(in_statefp) || '
                   AND soundex($1) = soundex(zip_state_loc.place)
             GROUP BY zip_state_loc.statefp,zip_state_loc.place
      UNION SELECT zip_lookup_base.statefp,zip_lookup_base.city As location,false As exact, array_agg(zip_lookup_base.zip),4
              FROM zip_lookup_base
             WHERE zip_lookup_base.statefp = ' || quote_nullable(in_statefp) || '
                         AND (soundex($1) = soundex(zip_lookup_base.city) OR soundex($1) = soundex(zip_lookup_base.county))
             GROUP BY zip_lookup_base.statefp,zip_lookup_base.city
      UNION SELECT ' || quote_nullable(in_statefp) || ' As statefp,$1 As location,false As exact,NULL, 5) as a '
      ' WHERE a.statefp IS NOT NULL
      GROUP BY statefp,location,a.zip,exact, pref ORDER BY exact desc, pref, zip';
  /** FOR zip_info IN     SELECT statefp,location,zip,exact,min(pref) FROM
    (SELECT zip_state.statefp as statefp,parsed.location as location, true As exact, ARRAY[zip_state.zip] as zip,1 as pref
        FROM zip_state WHERE zip_state.zip = parsed.zip
            AND (in_statefp IS NULL OR zip_state.statefp = in_statefp)
        UNION SELECT zip_state_loc.statefp,parsed.location,false As exact, array_agg(zip_state_loc.zip),2 + diff_zip(zip[1], parsed.zip)
              FROM zip_state_loc
             WHERE zip_state_loc.statefp = in_statefp
                   AND lower(parsed.location) = lower(zip_state_loc.place)
             GROUP BY zip_state_loc.statefp,parsed.location
      UNION SELECT zip_state_loc.statefp,parsed.location,false As exact, array_agg(zip_state_loc.zip),3
              FROM zip_state_loc
             WHERE zip_state_loc.statefp = in_statefp
                   AND soundex(parsed.location) = soundex(zip_state_loc.place)
             GROUP BY zip_state_loc.statefp,parsed.location
      UNION SELECT zip_lookup_base.statefp,parsed.location,false As exact, array_agg(zip_lookup_base.zip),4
              FROM zip_lookup_base
             WHERE zip_lookup_base.statefp = in_statefp
                         AND (soundex(parsed.location) = soundex(zip_lookup_base.city) OR soundex(parsed.location) = soundex(zip_lookup_base.county))
             GROUP BY zip_lookup_base.statefp,parsed.location
      UNION SELECT in_statefp,parsed.location,false As exact,NULL, 5) as a
        --JOIN (VALUES (true),(false)) as b(exact) on TRUE
      WHERE statefp IS NOT NULL
      GROUP BY statefp,location,zip,exact, pref ORDER BY exact desc, pref, zip  **/
  FOR zip_info IN EXECUTE var_sql USING parsed.location, parsed.zip, var_zip_penalty  LOOP
  -- For zip distance metric we consider both the distance of zip based on numeric as well aa levenshtein
  -- We use the prequalabr (these are like Old, that may or may not appear in front of the street name)
  -- We also treat pretypabr as fetype since in normalize we treat these as streetypes  and highways usually have the type here
  -- In pprint_addy we changed to put it in front if it is a is_hw type
    stmt := 'SELECT DISTINCT ON (sub.predirabrv,sub.fename,COALESCE(sub.suftypabrv, sub.pretypabrv) ,sub.sufdirabrv,coalesce(p.name,zip.city,cs.name,co.name),s.stusps,sub.zip)'
         || '    sub.predirabrv   as fedirp,'
         || '    sub.fename,'
         || '    COALESCE(sub.suftypabrv, sub.pretypabrv)   as fetype,'
         || '    sub.sufdirabrv   as fedirs,'
         || '    coalesce(p.name,zip.city,cs.name,co.name)::varchar as place,'
         || '    s.stusps as state,'
         || '    sub.zip as zip,'
         || '    interpolate_from_address($1, sub.fromhn,'
         || '        sub.tohn, e.the_geom, sub.side) as address_geom,'
         || '       (sub.sub_rating + '
         || CASE WHEN parsed.zip > '' THEN '  least((coalesce(diff_zip($7 , sub.zip),0) *$9)::integer, coalesce(levenshtein_ignore_case($7, sub.zip)*$9,0) ) '
            ELSE '3' END::text
         || ' + coalesce(least(levenshtein_ignore_case($3, coalesce(p.name,zip.city,cs.name,co.name)), levenshtein_ignore_case($3, coalesce(cs.name,co.name))),5) )::integer'
         || '    as sub_rating,'
         || '    sub.exact_address as exact_address '
         || ' FROM ('
         || '  SELECT a.tlid, predirabrv, COALESCE(a.prequalabr || '' '','''' ) || a.name As fename, suftypabrv, sufdirabrv, fromhn, tohn,
                    side, a.statefp, zip, rate_attributes($5, a.predirabrv,'
         || '    $2,  a.name , $4,'
         || '    a.suftypabrv , $6,'
         || '    a.sufdirabrv, a.prequalabr) + '
         || '    CASE '
         || '        WHEN $1::integer IS NULL OR b.fromhn IS NULL THEN 20'
         || '        WHEN $1::integer >= least_hn(b.fromhn, b.tohn) '
         || '            AND $1::integer <= greatest_hn(b.fromhn,b.tohn)'
         || '            AND ($1::integer % 2) = (to_number(b.fromhn,''99999999'') % 2)::integer'
         || '            THEN 0'
         || '        WHEN $1::integer >= least_hn(b.fromhn,b.tohn)'
         || '            AND $1::integer <= greatest_hn(b.fromhn,b.tohn)'
         || '            THEN 2'
         || '        ELSE'
         || '            ((1.0 - '
         ||              '(least_hn($1::text,least_hn(b.fromhn,b.tohn)::text)::numeric /'
         ||              ' greatest(1,greatest_hn($1::text,greatest_hn(b.fromhn,b.tohn)::text)))'
         ||              ') * 5)::integer + 5'
         || '        END'
         || '    as sub_rating,$1::integer >= least_hn(b.fromhn,b.tohn) '
         || '            AND $1::integer <= greatest_hn(b.fromhn,b.tohn) '
         || '            AND ($1 % 2)::numeric::integer = (to_number(b.fromhn,''99999999'') % 2)'
         || '    as exact_address, a.name, a.prequalabr, a.pretypabrv '
         || '  FROM tiger.featnames a join tiger.addr b ON (a.tlid = b.tlid AND a.statefp = b.statefp  )'
         || '  WHERE'
         || '        a.statefp = ' || quote_literal(zip_info.statefp) || ' AND a.mtfcc LIKE ''S%''  '
         || coalesce('    AND b.zip IN (''' || array_to_string(zip_info.zip,''',''') || ''') ','')
         || CASE WHEN zip_info.exact
                 THEN '    AND ( lower($2) = lower(a.name) OR  ( a.prequalabr > '''' AND trim(lower($2), lower(a.prequalabr) || '' '') = lower(a.name) ) OR numeric_streets_equal($2, a.name) ) '
                 ELSE '    AND ( soundex($2) = soundex(a.name)  OR ( (length($2) > 15 or (length($2) > 7 AND a.prequalabr > '''') ) AND lower(a.fullname) LIKE lower(substring($2,1,15)) || ''%'' ) OR  numeric_streets_equal($2, a.name) ) '
            END
         || '  ORDER BY 11'
         || '  LIMIT 200'
         || '    ) AS sub'
         || '  JOIN tiger.edges e ON (' || quote_literal(zip_info.statefp) || ' = e.statefp AND sub.tlid = e.tlid AND e.mtfcc LIKE ''S%'' '
         ||   CASE WHEN var_restrict_geom IS NOT NULL THEN ' AND ST_Intersects(e.the_geom, $8) '  ELSE '' END || ') '
         || '  JOIN tiger.state s ON (' || quote_literal(zip_info.statefp) || ' = s.statefp)'
         || '  JOIN tiger.faces f ON (' || quote_literal(zip_info.statefp) || ' = f.statefp AND (e.tfidl = f.tfid OR e.tfidr = f.tfid))'
         || '  LEFT JOIN tiger.zip_lookup_base zip ON (sub.zip = zip.zip AND zip.statefp=' || quote_literal(zip_info.statefp) || ')'
         || '  LEFT JOIN tiger.place p ON (' || quote_literal(zip_info.statefp) || ' = p.statefp AND f.placefp = p.placefp)'
         || '  LEFT JOIN tiger.county co ON (' || quote_literal(zip_info.statefp) || ' = co.statefp AND f.countyfp = co.countyfp)'
         || '  LEFT JOIN tiger.cousub cs ON (' || quote_literal(zip_info.statefp) || ' = cs.statefp AND cs.cosbidfp = sub.statefp || co.countyfp || f.cousubfp)'
         || ' WHERE'
         || '  ( (sub.side = ''L'' and e.tfidl = f.tfid) OR (sub.side = ''R'' and e.tfidr = f.tfid) ) '
         || ' ORDER BY 1,2,3,4,5,6,7,9'
         || ' LIMIT 10'
         ;
    IF var_debug THEN
        RAISE NOTICE '%', stmt;
        RAISE NOTICE 'PREPARE query_base_geo(integer, varchar,varchar,varchar,varchar,varchar,varchar,geometry,numeric) As %', stmt;
        RAISE NOTICE 'EXECUTE query_base_geo(%,%,%,%,%,%,%,%,%); ', parsed.address,quote_nullable(parsed.streetName), quote_nullable(parsed.location), quote_nullable(parsed.streetTypeAbbrev), quote_nullable(parsed.preDirAbbrev), quote_nullable(parsed.postDirAbbrev), quote_nullable(parsed.zip), quote_nullable(var_restrict_geom::text), quote_nullable(var_zip_penalty);
        RAISE NOTICE 'DEALLOCATE query_base_geo;';
    END IF;
    -- If we got an exact street match then when we hit the non-exact
    -- set of tests, just drop out.
    IF NOT zip_info.exact AND exact_street THEN
        RETURN;
    END IF;

    FOR results IN EXECUTE stmt USING parsed.address,parsed.streetName, parsed.location, parsed.streetTypeAbbrev, parsed.preDirAbbrev, parsed.postDirAbbrev, parsed.zip, var_restrict_geom, var_zip_penalty LOOP

      -- If we found a match with an exact street, then don't bother
      -- trying to do non-exact matches
      IF zip_info.exact THEN
        exact_street := true;
      END IF;

      IF results.exact_address THEN
        ADDY.address := substring(parsed.address::text FROM '[0-9]+')::integer;
      ELSE
        ADDY.address := NULL;
      END IF;

      ADDY.preDirAbbrev     := results.fedirp;
      ADDY.streetName       := results.fename;
      ADDY.streetTypeAbbrev := results.fetype;
      ADDY.postDirAbbrev    := results.fedirs;
      ADDY.location         := results.place;
      ADDY.stateAbbrev      := results.state;
      ADDY.zip              := results.zip;
      ADDY.parsed := TRUE;

      GEOMOUT := results.address_geom;
      RATING := results.sub_rating::integer;
      var_n := var_n + 1;

      -- If our ratings go above 99 exit because its a really bad match
      IF RATING > 99 THEN
        RETURN;
      END IF;

      RETURN NEXT;

      -- If we get an exact match, then just return that
      IF RATING = 0 THEN
        RETURN;
      END IF;

    END LOOP;
    IF var_n > max_results  THEN --we have exceeded our desired limit
        RETURN;
    END IF;
  END LOOP;

  RETURN;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.geocode_intersection(roadway1 text, roadway2 text, in_state text, in_city text DEFAULT ''::text, in_zip text DEFAULT ''::text, num_results integer DEFAULT 10, OUT addy norm_addy, OUT geomout geometry, OUT rating integer)
 RETURNS SETOF record
 LANGUAGE plpgsql
 IMMUTABLE COST 1000 ROWS 10
 SET join_collapse_limit TO '2'
AS $function$
DECLARE
    var_na_road norm_addy;
    var_na_inter1 norm_addy;
    var_sql text := '';
    var_zip varchar(5)[];
    in_statefp varchar(2) ;
    var_debug boolean := get_geocode_setting('debug_geocode_intersection')::boolean;
    results record;
BEGIN
    IF COALESCE(roadway1,'') = '' OR COALESCE(roadway2,'') = '' THEN
        -- not enough to give a result just return
        RETURN ;
    ELSE
        var_na_road := normalize_address('0 ' || roadway1 || ', ' || COALESCE(in_city,'') || ', ' || in_state || ' ' || in_zip);
        var_na_inter1  := normalize_address('0 ' || roadway2 || ', ' || COALESCE(in_city,'') || ', ' || in_state || ' ' || in_zip);
    END IF;
    in_statefp := statefp FROM state_lookup As s WHERE s.abbrev = upper(in_state);
    IF COALESCE(in_zip,'') > '' THEN -- limit search to 2 plus or minus the input zip
        var_zip := zip_range(in_zip, -2,2);
    END IF;

    IF var_zip IS NULL AND in_city > '' THEN
        var_zip := array_agg(zip) FROM zip_lookup_base WHERE statefp = in_statefp AND lower(city) = lower(in_city);
    END IF;

    -- if we don't have a city or zip, don't bother doing the zip check, just keep as null
    IF var_zip IS NULL AND in_city > '' THEN
        var_zip := array_agg(zip) FROM zip_lookup_base WHERE statefp = in_statefp AND lower(city) LIKE lower(in_city) || '%'  ;
    END IF;
    IF var_debug THEN
		RAISE NOTICE 'var_zip: %, city: %', quote_nullable(var_zip), quote_nullable(in_city);
    END IF;
    var_sql := '
    WITH
    	a1 AS (SELECT f.*, addr.fromhn, addr.tohn, addr.side , addr.zip
    				FROM (SELECT * FROM tiger.featnames
    							WHERE statefp = $1 AND ( lower(name) = $2  ' ||
    							CASE WHEN length(var_na_road.streetName) > 5 THEN ' or  lower(fullname) LIKE $6 || ''%'' ' ELSE '' END || ')'
    							|| ')  AS f LEFT JOIN (SELECT * FROM tiger.addr As addr WHERE addr.statefp = $1) As addr ON (addr.tlid = f.tlid AND addr.statefp = f.statefp)
    					WHERE $5::text[] IS NULL OR addr.zip = ANY($5::text[]) OR addr.zip IS NULL
    				ORDER BY CASE WHEN lower(f.fullname) = $6 THEN 0 ELSE 1 END
    				LIMIT 50000
    			  ),
        a2 AS (SELECT f.*, addr.fromhn, addr.tohn, addr.side , addr.zip
    				FROM (SELECT * FROM tiger.featnames
    							WHERE statefp = $1 AND ( lower(name) = $4 ' ||
    							CASE WHEN length(var_na_inter1.streetName) > 5 THEN ' or lower(fullname) LIKE $7 || ''%'' ' ELSE '' END || ')'
    							|| ' )  AS f LEFT JOIN (SELECT * FROM tiger.addr As addr WHERE addr.statefp = $1) AS addr ON (addr.tlid = f.tlid AND addr.statefp = f.statefp)
    					WHERE $5::text[] IS NULL OR addr.zip = ANY($5::text[])  or addr.zip IS NULL
    			ORDER BY CASE WHEN lower(f.fullname) = $7 THEN 0 ELSE 1 END
    				LIMIT 50000
    			  ),
    	 e1 AS (SELECT e.the_geom, e.tnidf, e.tnidt, a.*,
    	 			CASE WHEN a.side = ''L'' THEN e.tfidl ELSE e.tfidr END AS tfid
    	 			FROM a1 As a
    					INNER JOIN  tiger.edges AS e ON (e.statefp = a.statefp AND a.tlid = e.tlid)
    				WHERE e.statefp = $1
    				ORDER BY CASE WHEN lower(a.name) = $4 THEN 0 ELSE 1 END + CASE WHEN lower(e.fullname) = $7 THEN 0 ELSE 1 END
    				LIMIT 5000) ,
    	e2 AS (SELECT e.the_geom, e.tnidf, e.tnidt, a.*,
    	 			CASE WHEN a.side = ''L'' THEN e.tfidl ELSE e.tfidr END AS tfid
    				FROM (SELECT * FROM tiger.edges WHERE statefp = $1) AS e INNER JOIN a2 AS a ON (e.statefp = a.statefp AND a.tlid = e.tlid)
    					INNER JOIN e1 ON (e.statefp = e1.statefp
    					AND ARRAY[e.tnidf, e.tnidt] && ARRAY[e1.tnidf, e1.tnidt] )

    				WHERE (lower(e.fullname) = $7 or lower(a.name) LIKE $4 || ''%'')
    				ORDER BY CASE WHEN lower(a.name) = $4 THEN 0 ELSE 1 END + CASE WHEN lower(e.fullname) = $7 THEN 0 ELSE 1 END
    				LIMIT 5000
    				),
    	segs AS (SELECT DISTINCT ON(e1.tlid, e1.side)
                   CASE WHEN e1.tnidf = e2.tnidf OR e1.tnidf = e2.tnidt THEN
                                e1.fromhn
                            ELSE
                                e1.tohn END As address, e1.predirabrv As fedirp, COALESCE(e1.prequalabr || '' '','''' ) || e1.name As fename,
                             COALESCE(e1.suftypabrv,e1.pretypabrv)  As fetype, e1.sufdirabrv AS fedirs,
                               p.name As place, e1.zip,
                             CASE WHEN e1.tnidf = e2.tnidf OR e1.tnidf = e2.tnidt THEN
                                ST_StartPoint(ST_GeometryN(ST_Multi(e1.the_geom),1))
                             ELSE ST_EndPoint(ST_GeometryN(ST_Multi(e1.the_geom),1)) END AS geom ,
                                CASE WHEN lower(p.name) = $3 THEN 0 ELSE 1 END
                                + levenshtein_ignore_case(p.name, $3)
                                + levenshtein_ignore_case(e1.name || COALESCE('' '' || e1.sufqualabr, ''''),$2) +
                                CASE WHEN e1.fullname = $6 THEN 0 ELSE levenshtein_ignore_case(e1.fullname, $6) END +
                                + levenshtein_ignore_case(e2.name || COALESCE('' '' || e2.sufqualabr, ''''),$4)
                                AS a_rating
                    FROM e1
                            INNER JOIN e2 ON (
                                  ARRAY[e2.tnidf, e2.tnidt] && ARRAY[e1.tnidf, e1.tnidt]  )
                             INNER JOIN (SELECT * FROM tiger.faces WHERE statefp = $1) As fa1 ON (e1.tfid = fa1.tfid  )
                          LEFT JOIN tiger.place AS p ON (fa1.placefp = p.placefp AND p.statefp = $1 )
                       ORDER BY e1.tlid, e1.side, a_rating LIMIT $9*4 )
    SELECT address, fedirp , fename, fetype,fedirs,place, zip , geom, a_rating
        FROM segs ORDER BY a_rating LIMIT  $9';

    IF var_debug THEN
        RAISE NOTICE 'sql: %', replace(replace(replace(
        	replace(replace(replace(
                replace(
                    replace(
                        replace(var_sql, '$1', quote_nullable(in_statefp)),
                              '$2', quote_nullable(lower(var_na_road.streetName) ) ),
                      '$3', quote_nullable(lower(in_city)) ),
                      '$4', quote_nullable(lower(var_na_inter1.streetName) ) ),
                      '$5', quote_nullable(var_zip) ),
                      '$6', quote_nullable(lower(var_na_road.streetName || ' ' || COALESCE(var_na_road.streetTypeAbbrev,'') )) ) ,
                      '$7', quote_nullable(trim(lower(var_na_inter1.streetName || ' ' || COALESCE(var_na_inter1.streetTypeAbbrev,'') )) ) ) ,
		 '$8', quote_nullable(in_state ) ),  '$9', num_results::text );
    END IF;

    FOR results IN EXECUTE var_sql USING in_statefp, trim(lower(var_na_road.streetName)), lower(in_city), lower(var_na_inter1.streetName), var_zip,
		trim(lower(var_na_road.streetName || ' ' || COALESCE(var_na_road.streetTypeAbbrev,''))),
		trim(lower(var_na_inter1.streetName || ' ' || COALESCE(var_na_inter1.streetTypeAbbrev,''))), in_state, num_results LOOP
		ADDY.preDirAbbrev     := results.fedirp;
        ADDY.streetName       := results.fename;
        ADDY.streetTypeAbbrev := results.fetype;
        ADDY.postDirAbbrev    := results.fedirs;
        ADDY.location         := results.place;
        ADDY.stateAbbrev      := in_state;
        ADDY.zip              := results.zip;
        ADDY.parsed := TRUE;
        ADDY.address := substring(results.address FROM '[0-9]+')::integer;

        GEOMOUT := results.geom;
        RATING := results.a_rating;
		RETURN NEXT;
	END LOOP;
	RETURN;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.geocode_location(parsed norm_addy, restrict_geom geometry DEFAULT NULL::geometry, OUT addy norm_addy, OUT geomout geometry, OUT rating integer)
 RETURNS SETOF record
 LANGUAGE plpgsql
 STABLE
AS $function$
DECLARE
  result RECORD;
  in_statefp VARCHAR;
  stmt VARCHAR;
  var_debug boolean := false;
BEGIN

  in_statefp := statefp FROM state WHERE state.stusps = parsed.stateAbbrev;

  IF var_debug THEN
    RAISE NOTICE 'geocode_location starting: %', clock_timestamp();
  END IF;
  FOR result IN
    SELECT
        coalesce(zip.city)::varchar as place,
        zip.zip as zip,
        ST_Centroid(zcta5.the_geom) as address_geom,
        stusps as state,
        100::integer + coalesce(levenshtein_ignore_case(coalesce(zip.city), parsed.location),0) as in_rating
    FROM
      zip_lookup_base zip
      JOIN zcta5 ON (zip.zip = zcta5.zcta5ce AND zip.statefp = zcta5.statefp)
      JOIN state ON (state.statefp=zip.statefp)
    WHERE
      parsed.zip = zip.zip OR
      (soundex(zip.city) = soundex(parsed.location) and zip.statefp = in_statefp)
    ORDER BY levenshtein_ignore_case(coalesce(zip.city), parsed.location), zip.zip
  LOOP
    ADDY.location := result.place;
    ADDY.stateAbbrev := result.state;
    ADDY.zip := result.zip;
    ADDY.parsed := true;
    GEOMOUT := result.address_geom;
    RATING := result.in_rating;

    RETURN NEXT;

    IF RATING = 100 THEN
      RETURN;
    END IF;

  END LOOP;

  IF parsed.location IS NULL THEN
    parsed.location := city FROM zip_lookup_base WHERE zip_lookup_base.zip = parsed.zip ORDER BY zip_lookup_base.zip LIMIT 1;
    in_statefp := statefp FROM zip_lookup_base WHERE zip_lookup_base.zip = parsed.zip ORDER BY zip_lookup_base.zip LIMIT 1;
  END IF;

  stmt := 'SELECT '
       || ' pl.name as place, '
       || ' state.stusps as stateAbbrev, '
       || ' ST_Centroid(pl.the_geom) as address_geom, '
       || ' 100::integer + levenshtein_ignore_case(coalesce(pl.name), ' || quote_literal(coalesce(parsed.location,'')) || ') as in_rating '
       || ' FROM (SELECT * FROM place WHERE statefp = ' ||  quote_literal(coalesce(in_statefp,'')) || ' ' || COALESCE(' AND ST_Intersects(' || quote_literal(restrict_geom::text) || '::geometry, the_geom)', '') || ') AS pl '
       || ' INNER JOIN state ON(pl.statefp = state.statefp)'
       || ' WHERE soundex(pl.name) = soundex(' || quote_literal(coalesce(parsed.location,'')) || ') and pl.statefp = ' || quote_literal(COALESCE(in_statefp,''))
       || ' ORDER BY levenshtein_ignore_case(coalesce(pl.name), ' || quote_literal(coalesce(parsed.location,'')) || ');'
       ;

  IF var_debug THEN
    RAISE NOTICE 'geocode_location stmt: %', stmt;
  END IF;
  FOR result IN EXECUTE stmt
  LOOP

    ADDY.location := result.place;
    ADDY.stateAbbrev := result.stateAbbrev;
    ADDY.zip = parsed.zip;
    ADDY.parsed := true;
    GEOMOUT := result.address_geom;
    RATING := result.in_rating;

    RETURN NEXT;

    IF RATING = 100 THEN
      RETURN;
      IF var_debug THEN
        RAISE NOTICE 'geocode_location ending hit 100 rating result: %', clock_timestamp();
      END IF;
    END IF;
  END LOOP;

  IF var_debug THEN
    RAISE NOTICE 'geocode_location ending: %', clock_timestamp();
  END IF;

  RETURN;

END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.get_geocode_setting(setting_name text)
 RETURNS text
 LANGUAGE sql
 STABLE
AS $function$
SELECT COALESCE(gc.setting,gd.setting) As setting FROM geocode_settings_default AS gd LEFT JOIN geocode_settings AS gc ON gd.name = gc.name  WHERE gd.name = $1;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.get_last_words(inputstring character varying, count integer)
 RETURNS character varying
 LANGUAGE plpgsql
 IMMUTABLE COST 10
AS $function$
DECLARE
  tempString VARCHAR;
  result VARCHAR := '';
BEGIN
  FOR i IN 1..count LOOP
    tempString := substring(inputString from '((?: )+[a-zA-Z0-9_]*)' || result || '$');

    IF tempString IS NULL THEN
      RETURN inputString;
    END IF;

    result := tempString || result;
  END LOOP;

  result := trim(both from result);

  RETURN result;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.get_tract(loc_geom geometry, output_field text DEFAULT 'name'::text)
 RETURNS text
 LANGUAGE plpgsql
 IMMUTABLE COST 500
AS $function$
DECLARE
  var_state text := NULL;
  var_stusps text := NULL;
  var_result text := NULL;
  var_loc_geom geometry;
  var_stmt text;
  var_debug boolean = false;
BEGIN
	IF loc_geom IS NULL THEN
		RETURN null;
	ELSE
		IF ST_SRID(loc_geom) = 4269 THEN
			var_loc_geom := loc_geom;
		ELSIF ST_SRID(loc_geom) > 0 THEN
			var_loc_geom := ST_Transform(loc_geom, 4269);
		ELSE --If srid is unknown, assume its 4269
			var_loc_geom := ST_SetSRID(loc_geom, 4269);
		END IF;
		IF GeometryType(var_loc_geom) != 'POINT' THEN
			var_loc_geom := ST_Centroid(var_loc_geom);
		END IF;
	END IF;
	-- Determine state tables to check
	-- this is needed to take advantage of constraint exclusion
	IF var_debug THEN
		RAISE NOTICE 'Get matching states start: %', clock_timestamp();
	END IF;
	SELECT statefp, stusps INTO var_state, var_stusps FROM state WHERE ST_Intersects(the_geom, var_loc_geom) LIMIT 1;
	IF var_debug THEN
		RAISE NOTICE 'Get matching states end: % -  %', var_state, clock_timestamp();
	END IF;
	IF var_state IS NULL THEN
		-- We don't have any data for this state
		RAISE NOTICE 'No data for this state';
		RETURN NULL;
	END IF;
	-- locate county
	var_stmt := 'SELECT ' || quote_ident(output_field) || ' FROM tract WHERE statefp =  $1 AND ST_Intersects(the_geom, $2) LIMIT 1;';
	EXECUTE var_stmt INTO var_result USING var_state, var_loc_geom ;
	RETURN var_result;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.greatest_hn(fromhn character varying, tohn character varying)
 RETURNS integer
 LANGUAGE sql
 IMMUTABLE COST 200
AS $function$ SELECT greatest(to_number( CASE WHEN trim($1) ~ '^[0-9]+$' THEN $1 ELSE '0' END,'99999999'),to_number(CASE WHEN trim($2) ~ '^[0-9]+$' THEN $2 ELSE '0' END,'99999999') )::integer;  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.includes_address(given_address integer, addr1 integer, addr2 integer, addr3 integer, addr4 integer)
 RETURNS boolean
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
DECLARE
  lmaxaddr INTEGER := -1;
  rmaxaddr INTEGER := -1;
  lminaddr INTEGER := -1;
  rminaddr INTEGER := -1;
  maxaddr INTEGER := -1;
  minaddr INTEGER := -1;
  verbose BOOLEAN := false;
BEGIN
  IF addr1 IS NOT NULL THEN
    maxaddr := addr1;
    minaddr := addr1;
    lmaxaddr := addr1;
    lminaddr := addr1;
  END IF;

  IF addr2 IS NOT NULL THEN
    IF addr2 < minaddr OR minaddr = -1 THEN
      minaddr := addr2;
    END IF;
    IF addr2 > maxaddr OR maxaddr = -1 THEN
      maxaddr := addr2;
    END IF;
    IF addr2 > lmaxaddr OR lmaxaddr = -1 THEN
      lmaxaddr := addr2;
    END IF;
    IF addr2 < lminaddr OR lminaddr = -1 THEN
      lminaddr := addr2;
    END IF;
  END IF;

  IF addr3 IS NOT NULL THEN
    IF addr3 < minaddr OR minaddr = -1 THEN
      minaddr := addr3;
    END IF;
    IF addr3 > maxaddr OR maxaddr = -1 THEN
      maxaddr := addr3;
    END IF;
    rmaxaddr := addr3;
    rminaddr := addr3;
  END IF;

  IF addr4 IS NOT NULL THEN
    IF addr4 < minaddr OR minaddr = -1 THEN
      minaddr := addr4;
    END IF;
    IF addr4 > maxaddr OR maxaddr = -1 THEN
      maxaddr := addr4;
    END IF;
    IF addr4 > rmaxaddr OR rmaxaddr = -1 THEN
      rmaxaddr := addr4;
    END IF;
    IF addr4 < rminaddr OR rminaddr = -1 THEN
      rminaddr := addr4;
    END IF;
  END IF;

  IF minaddr = -1 OR maxaddr = -1 THEN
    -- No addresses were non-null, return FALSE (arbitrary)
    RETURN FALSE;
  ELSIF given_address >= minaddr AND given_address <= maxaddr THEN
    -- The address is within the given range
    IF given_address >= lminaddr AND given_address <= lmaxaddr THEN
      -- This checks to see if the address is on this side of the
      -- road, ie if the address is even, the street range must be even
      IF (given_address % 2) = (lminaddr % 2)
          OR (given_address % 2) = (lmaxaddr % 2) THEN
        RETURN TRUE;
      END IF;
    END IF;
    IF given_address >= rminaddr AND given_address <= rmaxaddr THEN
      -- See above
      IF (given_address % 2) = (rminaddr % 2)
          OR (given_address % 2) = (rmaxaddr % 2) THEN
        RETURN TRUE;
      END IF;
    END IF;
  END IF;
  -- The address is not within the range
  RETURN FALSE;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.install_geocode_settings()
 RETURNS void
 LANGUAGE plpgsql
AS $function$
DECLARE var_temp text;
BEGIN
	var_temp := tiger.SetSearchPathForInstall('tiger'); /** set set search path to have tiger in front **/
	IF NOT EXISTS(SELECT table_name FROM information_schema.columns WHERE table_schema = 'tiger' AND table_name = 'geocode_settings')  THEN
		CREATE TABLE geocode_settings(name text primary key, setting text, unit text, category text, short_desc text);
		GRANT SELECT ON geocode_settings TO public;
	END IF;
	IF NOT EXISTS(SELECT table_name FROM information_schema.columns WHERE table_schema = 'tiger' AND table_name = 'geocode_settings_default')  THEN
		CREATE TABLE geocode_settings_default(name text primary key, setting text, unit text, category text, short_desc text);
		GRANT SELECT ON geocode_settings_default TO public;
	END IF;
	--recreate defaults
	TRUNCATE TABLE geocode_settings_default;
	INSERT INTO geocode_settings_default(name,setting,unit,category,short_desc)
		SELECT f.*
		FROM
		(VALUES ('debug_geocode_address', 'false', 'boolean','debug', 'outputs debug information in notice log such as queries when geocode_addresss is called if true')
			, ('debug_geocode_intersection', 'false', 'boolean','debug', 'outputs debug information in notice log such as queries when geocode_intersection is called if true')
			, ('debug_normalize_address', 'false', 'boolean','debug', 'outputs debug information in notice log such as queries and intermediate expressions when normalize_address is called if true')
			, ('debug_reverse_geocode', 'false', 'boolean','debug', 'if true, outputs debug information in notice log such as queries and intermediate expressions when reverse_geocode')
			, ('reverse_geocode_numbered_roads', '0', 'integer','rating', 'For state and county highways, 0 - no preference in name, 1 - prefer the numbered highway name, 2 - prefer local state/county name')
			, ('use_pagc_address_parser', 'false', 'boolean','normalize', 'If set to true, will try to use the address_standardizer extension (via pagc_normalize_address) instead of tiger normalize_address built on')
			, ('zip_penalty', '2', 'numeric','rating', 'As input to rating will add (ref_zip - tar_zip)*zip_penalty where ref_zip is input address and tar_zip is a target address candidate')
		) f(name,setting,unit,category,short_desc);

	-- delete entries that are the same as default values
	DELETE FROM geocode_settings As gc USING geocode_settings_default As gf WHERE gf.name = gc.name AND gf.setting = gc.setting;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.install_missing_indexes()
 RETURNS boolean
 LANGUAGE plpgsql
AS $function$
DECLARE var_sql text = missing_indexes_generate_script();
BEGIN
	EXECUTE(var_sql);
	RETURN true;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.install_pagc_tables()
 RETURNS void
 LANGUAGE plpgsql
AS $function$
DECLARE var_temp text;
BEGIN
	var_temp := tiger.SetSearchPathForInstall('tiger'); /** set set search path to have tiger in front **/
	IF NOT EXISTS(SELECT table_name FROM information_schema.columns WHERE table_schema = 'tiger' AND table_name = 'pagc_gaz')  THEN
		CREATE TABLE pagc_gaz (id serial NOT NULL primary key ,seq integer ,word text, stdword text, token integer,is_custom boolean NOT NULL default true);
		GRANT SELECT ON pagc_gaz TO public;
	END IF;
	IF NOT EXISTS(SELECT table_name FROM information_schema.columns WHERE table_schema = 'tiger' AND table_name = 'pagc_lex')  THEN
		CREATE TABLE pagc_lex (id serial NOT NULL primary key,seq integer,word text,stdword text,token integer,is_custom boolean NOT NULL default true);
		GRANT SELECT ON pagc_lex TO public;
	END IF;
	IF NOT EXISTS(SELECT table_name FROM information_schema.columns WHERE table_schema = 'tiger' AND table_name = 'pagc_rules')  THEN
		CREATE TABLE pagc_rules (id serial NOT NULL primary key,rule text, is_custom boolean DEFAULT true);
		GRANT SELECT ON pagc_rules TO public;
	END IF;
	IF NOT EXISTS(SELECT table_name FROM information_schema.columns WHERE table_schema = 'tiger' AND table_name = 'pagc_gaz' AND data_type='text')  THEN
	-- its probably old table structure change type of lex and gaz columns
		ALTER TABLE tiger.pagc_lex ALTER COLUMN word TYPE text;
		ALTER TABLE tiger.pagc_lex ALTER COLUMN stdword TYPE text;
		ALTER TABLE tiger.pagc_gaz ALTER COLUMN word TYPE text;
		ALTER TABLE tiger.pagc_gaz ALTER COLUMN stdword TYPE text;
	END IF;
	IF NOT EXISTS(SELECT table_name FROM information_schema.columns WHERE table_schema = 'tiger' AND table_name = 'pagc_rules' AND column_name = 'is_custom' )  THEN
	-- its probably old table structure add column
		ALTER TABLE tiger.pagc_rules ADD COLUMN is_custom boolean NOT NULL DEFAULT false;
	END IF;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.interpolate_from_address(given_address integer, in_addr1 character varying, in_addr2 character varying, in_road geometry, in_side character varying DEFAULT ''::character varying, in_offset_m double precision DEFAULT 10)
 RETURNS geometry
 LANGUAGE plpgsql
 IMMUTABLE COST 10
 SET client_min_messages TO 'ERROR'
AS $function$
DECLARE
  addrwidth INTEGER;
  part DOUBLE PRECISION;
  road GEOMETRY;
  result GEOMETRY;
  var_addr1 INTEGER; var_addr2 INTEGER;
  center_pt GEOMETRY; cl_pt GEOMETRY;
  npos integer;
  delx float; dely float;  x0 float; y0 float; x1 float; y1 float; az float;
  var_dist float; dir integer;
BEGIN
    IF in_road IS NULL THEN
        RETURN NULL;
    END IF;

	var_addr1 := to_number( CASE WHEN in_addr1 ~ '^[0-9]+$' THEN in_addr1 ELSE '0' END, '999999');
	var_addr2 := to_number( CASE WHEN in_addr2 ~ '^[0-9]+$' THEN in_addr2 ELSE '0' END, '999999');

    IF geometrytype(in_road) = 'LINESTRING' THEN
      road := ST_Transform(in_road, utmzone(ST_StartPoint(in_road)) );
    ELSIF geometrytype(in_road) = 'MULTILINESTRING' THEN
    	road := ST_GeometryN(in_road,1);
    	road := ST_Transform(road, utmzone(ST_StartPoint(road)) );
    ELSE
      RETURN NULL;
    END IF;

    addrwidth := greatest(var_addr1,var_addr2) - least(var_addr1,var_addr2);
    IF addrwidth = 0 or addrwidth IS NULL THEN
        addrwidth = 1;
    END IF;
    part := (given_address - least(var_addr1,var_addr2)) / trunc(addrwidth, 1);

    IF var_addr1 > var_addr2 THEN
        part := 1 - part;
    END IF;

    IF part < 0 OR part > 1 OR part IS NULL THEN
        part := 0.5;
    END IF;

    center_pt = ST_LineInterpolatePoint(road, part);
    IF in_side > '' AND in_offset_m > 0 THEN
    /** Compute point the point to the in_side of the geometry **/
    /**Take into consideration non-straight so we consider azimuth
    	of the 2 points that straddle the center location**/
    	IF part = 0 THEN
    		az := ST_Azimuth (ST_StartPoint(road), ST_PointN(road,2));
    	ELSIF part = 1 THEN
    		az := ST_Azimuth (ST_PointN(road,ST_NPoints(road) - 1), ST_EndPoint(road));
    	ELSE
    		/** Find the largest nth point position that is before the center point
    			This will be the start of our azimuth calc **/
    		SELECT i INTO npos
    			FROM generate_series(1,ST_NPoints(road)) As i
    					WHERE part > ST_LineLocatePoint(road,ST_PointN(road,i))
    					ORDER BY i DESC;
    		IF npos < ST_NPoints(road) THEN
    			az := ST_Azimuth (ST_PointN(road,npos), ST_PointN(road, npos + 1));
    		ELSE
    			az := ST_Azimuth (center_pt, ST_PointN(road, npos));
    		END IF;
    	END IF;

        dir := CASE WHEN az < pi() THEN -1 ELSE 1 END;
        --dir := 1;
        var_dist := in_offset_m*CASE WHEN in_side = 'L' THEN -1 ELSE 1 END;
        delx := ABS(COS(az)) * var_dist * dir;
        dely := ABS(SIN(az)) * var_dist * dir;
        IF az > pi()/2 AND az < pi() OR az > 3 * pi()/2 THEN
			result := ST_Translate(center_pt, delx, dely) ;
		ELSE
			result := ST_Translate(center_pt, -delx, dely);
		END IF;
    ELSE
    	result := center_pt;
    END IF;
    result :=  ST_Transform(result, ST_SRID(in_road));
    --RAISE NOTICE 'start: %, center: %, new: %, side: %, offset: %, az: %', ST_AsText(ST_Transform(ST_StartPoint(road),ST_SRID(in_road))), ST_AsText(ST_Transform(center_pt,ST_SRID(in_road))),ST_AsText(result), in_side, in_offset_m, az;
    RETURN result;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.is_pretype(text)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
    SELECT EXISTS(SELECT name FROM street_type_lookup WHERE upper(name) = upper($1) AND is_hw );
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.least_hn(fromhn character varying, tohn character varying)
 RETURNS integer
 LANGUAGE sql
 IMMUTABLE COST 200
AS $function$ SELECT least(to_number( CASE WHEN trim($1) ~ '^[0-9]+$' THEN $1 ELSE '0' END,'9999999'),to_number(CASE WHEN trim($2) ~ '^[0-9]+$' THEN $2 ELSE '0' END,'9999999') )::integer;  $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.levenshtein_ignore_case(character varying, character varying)
 RETURNS integer
 LANGUAGE sql
 IMMUTABLE
AS $function$
  SELECT levenshtein(COALESCE(upper($1),''), COALESCE(upper($2),''));
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.loader_generate_census_script(param_states text[], os text)
 RETURNS SETOF text
 LANGUAGE sql
AS $function$
SELECT create_census_base_tables();
SELECT
	loader_macro_replace(
		replace(
			loader_macro_replace(declare_sect
				, ARRAY['staging_fold', 'state_fold','website_root', 'psql', 'state_abbrev', 'data_schema', 'staging_schema', 'state_fips'],
				ARRAY[variables.staging_fold, s.state_fold, variables.website_root, platform.psql, s.state_abbrev, variables.data_schema, variables.staging_schema, s.state_fips::text]
			), '/', platform.path_sep) || '
' ||
	-- State level files - if an override website is specified we use that instead of variable one
	array_to_string( ARRAY(SELECT 'cd ' || replace(variables.staging_fold,'/', platform.path_sep) || '
' || platform.wget || ' ' || COALESCE(lu.website_root_override,variables.website_root || '/' || upper(lookup_name)  ) || '/tl_' || variables.tiger_year || '_' || s.state_fips || '_' || lower(table_name) || '.zip --mirror --reject=html
'
|| 'cd ' ||  replace(variables.staging_fold,'/', platform.path_sep) || '/' || replace(regexp_replace(COALESCE(lu.website_root_override,variables.website_root || '/' || upper(lookup_name) ), 'http[s]+://', ''),'ftp://','')    || '
' || replace(platform.unzip_command, '*.zip', 'tl_' || variables.tiger_year || '_' || s.state_fips || '*_' || table_name || '.zip ') || '
' ||loader_macro_replace(COALESCE(lu.pre_load_process || E'\n', '') || platform.loader || ' -D -' ||  lu.insert_mode || ' -s 4269 -g the_geom '
		|| CASE WHEN lu.single_geom_mode THEN ' -S ' ELSE ' ' END::text || ' -W "latin1" tl_' || variables.tiger_year || '_' || s.state_fips
	|| '_' || lu.table_name || '.dbf tiger_staging.' || lower(s.state_abbrev) || '_' || lu.table_name || ' | '::text || platform.psql
		|| COALESCE(E'\n' ||
			lu.post_load_process , '') , ARRAY['loader','table_name', 'lookup_name'], ARRAY[platform.loader, lu.table_name, lu.lookup_name ])
				FROM loader_lookuptables AS lu
				WHERE level_state = true AND lu.lookup_name IN('bg','tract', 'tabblock')
				ORDER BY process_order, lookup_name), E'\n') ::text
	-- County Level files
	|| E'\n' ||
		array_to_string( ARRAY(SELECT 'cd ' || replace(variables.staging_fold,'/', platform.path_sep) || '
' ||
-- explode county files create wget call for each county file
array_to_string (ARRAY(SELECT platform.wget || ' --mirror  ' || COALESCE(lu.website_root_override,variables.website_root || '/' || upper(lookup_name)  ) || '/tl_' || variables.tiger_year || '_' || s.state_fips || c.countyfp || '_' || lower(table_name) || '.zip ' || E'\n'  AS county_out
FROM tiger.county As c
WHERE c.statefp = s.state_fips), ' ')
|| 'cd ' ||  replace(variables.staging_fold,'/', platform.path_sep) || '/' || replace(regexp_replace(COALESCE(lu.website_root_override,variables.website_root || '/' || upper(lookup_name)  || '/'), 'http[s]+://', ''),'ftp://','')  || '
' || replace(platform.unzip_command, '*.zip', 'tl_*_' || s.state_fips || '*_' || table_name || '*.zip ') || '
' || loader_macro_replace(COALESCE(lu.pre_load_process || E'\n', '') || COALESCE(county_process_command || E'\n','')
				|| COALESCE(E'\n' ||lu.post_load_process , '') , ARRAY['loader','table_name','lookup_name'], ARRAY[platform.loader  || ' -D ' || CASE WHEN lu.single_geom_mode THEN ' -S' ELSE ' ' END::text, lu.table_name, lu.lookup_name ])
				FROM loader_lookuptables AS lu
				WHERE level_county = true AND lu.lookup_name IN('bg','tract', 'tabblock')
				ORDER BY process_order, lookup_name), E'\n') ::text
	, ARRAY['psql', 'data_schema','staging_schema', 'staging_fold', 'state_fold', 'website_root', 'state_abbrev','state_fips'],
	ARRAY[platform.psql,  variables.data_schema, variables.staging_schema, variables.staging_fold, s.state_fold,variables.website_root, s.state_abbrev, s.state_fips::text])
			AS shell_code
FROM loader_variables As variables
		CROSS JOIN (SELECT name As state, abbrev As state_abbrev, lpad(st_code::text,2,'0') As state_fips,
			 lpad(st_code::text,2,'0') || '_'
	|| replace(name, ' ', '_') As state_fold
FROM state_lookup) As s CROSS JOIN loader_platform As platform
WHERE $1 @> ARRAY[state_abbrev::text]      -- If state is contained in list of states input generate script for it
AND platform.os = $2  -- generate script for selected platform
;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.loader_generate_nation_script(os text)
 RETURNS SETOF text
 LANGUAGE sql
AS $function$
WITH lu AS (SELECT lookup_name, table_name, pre_load_process,post_load_process, process_order, insert_mode, single_geom_mode, level_nation, level_county, level_state
    FROM  loader_lookuptables
				WHERE level_nation = true AND load = true)
SELECT
	loader_macro_replace(
		replace(
			loader_macro_replace(declare_sect
				, ARRAY['staging_fold', 'website_root', 'psql',  'data_schema', 'staging_schema'],
				ARRAY[variables.staging_fold, variables.website_root, platform.psql, variables.data_schema, variables.staging_schema]
			), '/', platform.path_sep) || '
'  ||
	-- Nation level files
	array_to_string( ARRAY(SELECT loader_macro_replace('cd ' || replace(variables.staging_fold,'/', platform.path_sep) || '
' || platform.wget || ' ' || variables.website_root  || '/'

-- hardcoding zcta5 path since doesn't follow convention
|| upper(CASE WHEN table_name = 'zcta510' THEN 'zcta5' ELSE table_name END)  || '/tl_' || variables.tiger_year || '_us_' || lower(table_name) || '.zip --mirror --reject=html
'
|| 'cd ' ||  replace(variables.staging_fold,'/', platform.path_sep) || '/' || replace(regexp_replace(variables.website_root, 'http[s]?://', ''),'ftp://','')  || '/'
-- note have to hard-code folder path for zcta because doesn't follow convention
|| upper(CASE WHEN table_name = 'zcta510' THEN 'zcta5' ELSE table_name END)  || '
' || replace(platform.unzip_command, '*.zip', 'tl_*' || table_name || '.zip ') || '
' || COALESCE(lu.pre_load_process || E'\n', '') || platform.loader || ' -D -' ||  lu.insert_mode || ' -s 4269 -g the_geom '
		|| CASE WHEN lu.single_geom_mode THEN ' -S ' ELSE ' ' END::text || ' -W "latin1" tl_' || variables.tiger_year
	|| '_us_' || lu.table_name || '.dbf tiger_staging.' || lu.table_name || ' | '::text || platform.psql
		|| COALESCE(E'\n' ||
			lu.post_load_process , '') , ARRAY['loader','table_name', 'lookup_name'], ARRAY[platform.loader, lu.table_name, lu.lookup_name ]
			)
				FROM lu
				ORDER BY process_order, lookup_name), E'\n') ::text
	, ARRAY['psql', 'data_schema','staging_schema', 'staging_fold', 'website_root'],
	ARRAY[platform.psql,  variables.data_schema, variables.staging_schema, variables.staging_fold, variables.website_root])
			AS shell_code
FROM loader_variables As variables
	 CROSS JOIN loader_platform As platform
WHERE platform.os = $1 -- generate script for selected platform
;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.loader_generate_script(param_states text[], os text)
 RETURNS SETOF text
 LANGUAGE sql
AS $function$
SELECT
	loader_macro_replace(
		replace(
			loader_macro_replace(declare_sect
				, ARRAY['staging_fold', 'state_fold','website_root', 'psql', 'state_abbrev', 'data_schema', 'staging_schema', 'state_fips'],
				ARRAY[variables.staging_fold, s.state_fold, variables.website_root, platform.psql, s.state_abbrev, variables.data_schema, variables.staging_schema, s.state_fips::text]
			), '/', platform.path_sep) || '
' ||
	-- State level files - if an override website is specified we use that instead of variable one
	array_to_string( ARRAY(SELECT 'cd ' || replace(variables.staging_fold,'/', platform.path_sep) || '
' || platform.wget || ' ' || COALESCE(lu.website_root_override,variables.website_root || '/' || upper(lookup_name)  ) || '/tl_' || variables.tiger_year || '_' || s.state_fips || '_' || lower(table_name) || '.zip --mirror --reject=html
'
|| 'cd ' ||  replace(variables.staging_fold,'/', platform.path_sep) || '/' || replace(regexp_replace(COALESCE(lu.website_root_override, variables.website_root || '/' || upper(lookup_name) ), 'http[s]?://', ''),'ftp://','')    || '
' || replace(platform.unzip_command, '*.zip', 'tl_' || variables.tiger_year || '_' || s.state_fips || '*_' || table_name || '.zip ') || '
' ||loader_macro_replace(COALESCE(lu.pre_load_process || E'\n', '') || platform.loader || ' -D -' ||  lu.insert_mode || ' -s 4269 -g the_geom '
		|| CASE WHEN lu.single_geom_mode THEN ' -S ' ELSE ' ' END::text || ' -W "latin1" tl_' || variables.tiger_year || '_' || s.state_fips
	|| '_' || lu.table_name || '.dbf tiger_staging.' || lower(s.state_abbrev) || '_' || lu.table_name || ' | '::text || platform.psql
		|| COALESCE(E'\n' ||
			lu.post_load_process , '') , ARRAY['loader','table_name', 'lookup_name'], ARRAY[platform.loader, lu.table_name, lu.lookup_name ])
				FROM loader_lookuptables AS lu
				WHERE level_state = true AND load = true
				ORDER BY process_order, lookup_name), E'\n') ::text
	-- County Level files
	|| E'\n' ||
		array_to_string( ARRAY(SELECT 'cd ' || replace(variables.staging_fold,'/', platform.path_sep) || '
' ||
-- explode county files create wget call for each county file
array_to_string (ARRAY(SELECT platform.wget || ' --mirror  ' || COALESCE(lu.website_root_override, variables.website_root || '/' || upper(lookup_name)  ) || '/tl_' || variables.tiger_year || '_' || s.state_fips || c.countyfp || '_' || lower(table_name) || '.zip ' || E'\n'  AS county_out
FROM tiger.county As c
WHERE c.statefp = s.state_fips), ' ')
|| 'cd ' ||  replace(variables.staging_fold,'/', platform.path_sep) || '/' || replace(regexp_replace(COALESCE(lu.website_root_override,variables.website_root || '/' || upper(lookup_name)  || '/'), 'http[s]?://', ''),'ftp://','')  || '
' || replace(platform.unzip_command, '*.zip', 'tl_*_' || s.state_fips || '*_' || table_name || '*.zip ') || '
' || loader_macro_replace(COALESCE(lu.pre_load_process || E'\n', '') || COALESCE(county_process_command || E'\n','')
				|| COALESCE(E'\n' ||lu.post_load_process , '') , ARRAY['loader','table_name','lookup_name'], ARRAY[platform.loader  || ' -D ' || CASE WHEN lu.single_geom_mode THEN ' -S' ELSE ' ' END::text, lu.table_name, lu.lookup_name ])
				FROM loader_lookuptables AS lu
				WHERE level_county = true AND load = true
				ORDER BY process_order, lookup_name), E'\n') ::text
	, ARRAY['psql', 'data_schema','staging_schema', 'staging_fold', 'state_fold', 'website_root', 'state_abbrev','state_fips'],
	ARRAY[platform.psql,  variables.data_schema, variables.staging_schema, variables.staging_fold, s.state_fold,variables.website_root, s.state_abbrev, s.state_fips::text])
			AS shell_code
FROM loader_variables As variables
		CROSS JOIN (SELECT name As state, abbrev As state_abbrev, lpad(st_code::text,2,'0') As state_fips,
			 lpad(st_code::text,2,'0') || '_'
	|| replace(name, ' ', '_') As state_fold
FROM state_lookup) As s CROSS JOIN loader_platform As platform
WHERE $1 @> ARRAY[state_abbrev::text]      -- If state is contained in list of states input generate script for it
AND platform.os = $2  -- generate script for selected platform
;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.loader_load_staged_data(param_staging_table text, param_target_table text, param_columns_exclude text[])
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
DECLARE
	var_sql text;
	var_staging_schema text; var_data_schema text;
	var_temp text;
	var_num_records bigint;
BEGIN
-- Add all the fields except geoid and gid
-- Assume all the columns are in same order as target
	SELECT staging_schema, data_schema INTO var_staging_schema, var_data_schema FROM loader_variables;
	var_sql := 'INSERT INTO ' || var_data_schema || '.' || quote_ident(param_target_table) || '(' ||
			array_to_string(ARRAY(SELECT quote_ident(column_name::text)
				FROM information_schema.columns
				 WHERE table_name = param_target_table
					AND table_schema = var_data_schema
					AND column_name <> ALL(param_columns_exclude)
                    ORDER BY column_name ), ',') || ') SELECT '
					|| array_to_string(ARRAY(SELECT quote_ident(column_name::text)
				FROM information_schema.columns
				 WHERE table_name = param_staging_table
					AND table_schema = var_staging_schema
					AND column_name <> ALL( param_columns_exclude)
                    ORDER BY column_name ), ',') ||' FROM '
					|| var_staging_schema || '.' || param_staging_table || ';';
	RAISE NOTICE '%', var_sql;
	EXECUTE (var_sql);
	GET DIAGNOSTICS var_num_records = ROW_COUNT;
	SELECT DropGeometryTable(var_staging_schema,param_staging_table) INTO var_temp;
	RETURN var_num_records;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.loader_load_staged_data(param_staging_table text, param_target_table text)
 RETURNS integer
 LANGUAGE sql
AS $function$
-- exclude this set list of columns if no exclusion list is specified

   SELECT  loader_load_staged_data($1, $2,(SELECT COALESCE(columns_exclude,ARRAY['gid', 'geoid','cpi','suffix1ce', 'statefp00', 'statefp10', 'countyfp00','countyfp10'
   ,'tractce00','tractce10', 'blkgrpce00', 'blkgrpce10', 'blockce00', 'blockce10'
      , 'cousubfp00', 'submcdfp00', 'conctyfp00', 'placefp00', 'aiannhfp00', 'aiannhce00',
       'comptyp00', 'trsubfp00', 'trsubce00', 'anrcfp00', 'elsdlea00', 'scsdlea00',
       'unsdlea00', 'uace00', 'cd108fp', 'sldust00', 'sldlst00', 'vtdst00', 'zcta5ce00',
       'tazce00', 'ugace00', 'puma5ce00','vtdst10','tazce10','uace10','puma5ce10','tazce', 'uace', 'vtdst', 'zcta5ce', 'zcta5ce10', 'puma5ce', 'ugace10','pumace10', 'estatefp', 'ugace', 'blockce']) FROM loader_lookuptables WHERE $2 LIKE '%' || lookup_name))
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.loader_macro_replace(param_input text, param_keys text[], param_values text[])
 RETURNS text
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
	DECLARE var_result text = param_input;
	DECLARE var_count integer = array_upper(param_keys,1);
	BEGIN
		FOR i IN 1..var_count LOOP
			var_result := replace(var_result, '${' || param_keys[i] || '}', param_values[i]);
		END LOOP;
		return var_result;
	END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.location_extract(fullstreet character varying, stateabbrev character varying)
 RETURNS character varying
 LANGUAGE plpgsql
 STABLE
AS $function$
DECLARE
  ws VARCHAR;
  location VARCHAR;
  lstate VARCHAR;
  stmt VARCHAR;
  street_array text[];
  word_count INTEGER;
  rec RECORD;
  best INTEGER := 0;
  tempString VARCHAR;
BEGIN
  IF fullStreet IS NULL THEN
    RETURN NULL;
  END IF;

  ws := E'[ ,.\n\f\t]';

  IF stateAbbrev IS NOT NULL THEN
    lstate := statefp FROM state_lookup WHERE abbrev = stateAbbrev;
  END IF;
  lstate := COALESCE(lstate,'');

  street_array := regexp_split_to_array(fullStreet,ws);
  word_count := array_upper(street_array,1);

  tempString := '';
  FOR i IN 1..word_count LOOP
    CONTINUE WHEN street_array[word_count-i+1] IS NULL OR street_array[word_count-i+1] = '';

    tempString := COALESCE(street_array[word_count-i+1],'') || tempString;

    stmt := ' SELECT'
         || '   1,'
         || '   name,'
         || '   levenshtein_ignore_case(' || quote_literal(tempString) || ',name) as rating,'
         || '   length(name) as len'
         || ' FROM place'
         || ' WHERE ' || CASE WHEN stateAbbrev IS NOT NULL THEN 'statefp = ' || quote_literal(lstate) || ' AND ' ELSE '' END
         || '   soundex(' || quote_literal(tempString) || ') = soundex(name)'
         || '   AND levenshtein_ignore_case(' || quote_literal(tempString) || ',name) <= 2 '
         || ' UNION ALL SELECT'
         || '   2,'
         || '   name,'
         || '   levenshtein_ignore_case(' || quote_literal(tempString) || ',name) as rating,'
         || '   length(name) as len'
         || ' FROM cousub'
         || ' WHERE ' || CASE WHEN stateAbbrev IS NOT NULL THEN 'statefp = ' || quote_literal(lstate) || ' AND ' ELSE '' END
         || '   soundex(' || quote_literal(tempString) || ') = soundex(name)'
         || '   AND levenshtein_ignore_case(' || quote_literal(tempString) || ',name) <= 2 '
         || ' ORDER BY '
         || '   3 ASC, 1 ASC, 4 DESC'
         || ' LIMIT 1;'
         ;

    EXECUTE stmt INTO rec;

    IF rec.rating >= best THEN
      location := tempString;
      best := rec.rating;
    END IF;

    tempString := ' ' || tempString;
  END LOOP;

  location := replace(location,' ',ws || '+');
  location := substring(fullStreet,'(?i)' || location || '$');

  RETURN location;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.location_extract_countysub_exact(fullstreet character varying, stateabbrev character varying)
 RETURNS character varying
 LANGUAGE plpgsql
 STABLE COST 10
AS $function$
DECLARE
  ws VARCHAR;
  location VARCHAR;
  tempInt INTEGER;
  lstate VARCHAR;
  rec RECORD;
BEGIN
  ws := E'[ ,.\n\f\t]';

  -- No hope of determining the location from place. Try countysub.
  IF stateAbbrev IS NOT NULL THEN
    lstate := statefp FROM state WHERE stusps = stateAbbrev;
    SELECT INTO tempInt count(*) FROM cousub
        WHERE cousub.statefp = lstate
        AND texticregexeq(fullStreet, '(?i)' || name || '$');
  ELSE
    SELECT INTO tempInt count(*) FROM cousub
        WHERE texticregexeq(fullStreet, '(?i)' || name || '$');
  END IF;

  IF tempInt > 0 THEN
    IF stateAbbrev IS NOT NULL THEN
      FOR rec IN SELECT substring(fullStreet, '(?i)('
          || name || ')$') AS value, name FROM cousub
          WHERE cousub.statefp = lstate
          AND texticregexeq(fullStreet, '(?i)' || ws || name ||
          '$') ORDER BY length(name) DESC LOOP
        -- Only the first result is needed.
        location := rec.value;
        EXIT;
      END LOOP;
    ELSE
      FOR rec IN SELECT substring(fullStreet, '(?i)('
          || name || ')$') AS value, name FROM cousub
          WHERE texticregexeq(fullStreet, '(?i)' || ws || name ||
          '$') ORDER BY length(name) DESC LOOP
        -- again, only the first is needed.
        location := rec.value;
        EXIT;
      END LOOP;
    END IF;
  END IF;

  RETURN location;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.location_extract_countysub_fuzzy(fullstreet character varying, stateabbrev character varying)
 RETURNS character varying
 LANGUAGE plpgsql
AS $function$
DECLARE
  ws VARCHAR;
  tempString VARCHAR;
  location VARCHAR;
  tempInt INTEGER;
  word_count INTEGER;
  rec RECORD;
  test BOOLEAN;
  lstate VARCHAR;
BEGIN
  ws := E'[ ,.\n\f\t]';

  -- Fuzzy matching.
  tempString := substring(fullStreet, '(?i)' || ws ||
      '([a-zA-Z0-9]+)$');
  IF tempString IS NULL THEN
    tempString := fullStreet;
  END IF;

  IF stateAbbrev IS NOT NULL THEN
    lstate := statefp FROM state WHERE stusps = stateAbbrev;
    SELECT INTO tempInt count(*) FROM cousub
        WHERE cousub.statefp = lstate
        AND soundex(tempString) = end_soundex(name);
  ELSE
    SELECT INTO tempInt count(*) FROM cousub
        WHERE soundex(tempString) = end_soundex(name);
  END IF;

  IF tempInt > 0 THEN
    tempInt := 50;
    -- Some potentials were found.  Begin a word-by-word soundex on each.
    IF stateAbbrev IS NOT NULL THEN
      FOR rec IN SELECT name FROM cousub
          WHERE cousub.statefp = lstate
          AND soundex(tempString) = end_soundex(name) LOOP
        word_count := count_words(rec.name);
        test := TRUE;
        tempString := get_last_words(fullStreet, word_count);
        FOR i IN 1..word_count LOOP
          IF soundex(split_part(tempString, ' ', i)) !=
            soundex(split_part(rec.name, ' ', i)) THEN
            test := FALSE;
          END IF;
        END LOOP;
        IF test THEN
          -- The soundex matched, determine if the distance is better.
          IF levenshtein_ignore_case(rec.name, tempString) < tempInt THEN
                location := tempString;
            tempInt := levenshtein_ignore_case(rec.name, tempString);
          END IF;
        END IF;
      END LOOP;
    ELSE
      FOR rec IN SELECT name FROM cousub
          WHERE soundex(tempString) = end_soundex(name) LOOP
        word_count := count_words(rec.name);
        test := TRUE;
        tempString := get_last_words(fullStreet, word_count);
        FOR i IN 1..word_count LOOP
          IF soundex(split_part(tempString, ' ', i)) !=
            soundex(split_part(rec.name, ' ', i)) THEN
            test := FALSE;
          END IF;
        END LOOP;
        IF test THEN
          -- The soundex matched, determine if the distance is better.
          IF levenshtein_ignore_case(rec.name, tempString) < tempInt THEN
                location := tempString;
            tempInt := levenshtein_ignore_case(rec.name, tempString);
          END IF;
        END IF;
      END LOOP;
    END IF;
  END IF; -- If no fuzzys were found, leave location null.

  RETURN location;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.location_extract_place_exact(fullstreet character varying, stateabbrev character varying)
 RETURNS character varying
 LANGUAGE plpgsql
 STABLE
AS $function$
DECLARE
  ws VARCHAR;
  location VARCHAR;
  tempInt INTEGER;
  lstate VARCHAR;
  rec RECORD;
BEGIN
  ws := E'[ ,.\n\f\t]';

  -- Try for an exact match against places
  IF stateAbbrev IS NOT NULL THEN
    lstate := statefp FROM state WHERE stusps = stateAbbrev;
    SELECT INTO tempInt count(*) FROM place
        WHERE place.statefp = lstate AND fullStreet ILIKE '%' || name || '%'
        AND texticregexeq(fullStreet, '(?i)' || name || '$');
  ELSE
    SELECT INTO tempInt count(*) FROM place
        WHERE fullStreet ILIKE '%' || name || '%' AND
        	texticregexeq(fullStreet, '(?i)' || name || '$');
  END IF;

  IF tempInt > 0 THEN
    -- Some matches were found.  Look for the last one in the string.
    IF stateAbbrev IS NOT NULL THEN
      FOR rec IN SELECT substring(fullStreet, '(?i)('
          || name || ')$') AS value, name FROM place
          WHERE place.statefp = lstate AND fullStreet ILIKE '%' || name || '%'
          AND texticregexeq(fullStreet, '(?i)'
          || name || '$') ORDER BY length(name) DESC LOOP
        -- Since the regex is end of string, only the longest (first) result
        -- is useful.
        location := rec.value;
        EXIT;
      END LOOP;
    ELSE
      FOR rec IN SELECT substring(fullStreet, '(?i)('
          || name || ')$') AS value, name FROM place
          WHERE fullStreet ILIKE '%' || name || '%' AND texticregexeq(fullStreet, '(?i)'
          || name || '$') ORDER BY length(name) DESC LOOP
        -- Since the regex is end of string, only the longest (first) result
        -- is useful.
        location := rec.value;
        EXIT;
      END LOOP;
    END IF;
  END IF;

  RETURN location;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.location_extract_place_fuzzy(fullstreet character varying, stateabbrev character varying)
 RETURNS character varying
 LANGUAGE plpgsql
 STABLE
AS $function$
DECLARE
  ws VARCHAR;
  tempString VARCHAR;
  location VARCHAR;
  tempInt INTEGER;
  word_count INTEGER;
  rec RECORD;
  test BOOLEAN;
  lstate VARCHAR;
BEGIN
  ws := E'[ ,.\n\f\t]';

  tempString := substring(fullStreet, '(?i)' || ws
      || '([a-zA-Z0-9]+)$');
  IF tempString IS NULL THEN
      tempString := fullStreet;
  END IF;

  IF stateAbbrev IS NOT NULL THEN
    lstate := statefp FROM state WHERE stusps = stateAbbrev;
    SELECT into tempInt count(*) FROM place
        WHERE place.statefp = lstate
        AND soundex(tempString) = end_soundex(name);
  ELSE
    SELECT into tempInt count(*) FROM place
        WHERE soundex(tempString) = end_soundex(name);
  END IF;

  IF tempInt > 0 THEN
    -- Some potentials were found.  Begin a word-by-word soundex on each.
    tempInt := 50;
    IF stateAbbrev IS NOT NULL THEN
      FOR rec IN SELECT name FROM place
          WHERE place.statefp = lstate
          AND soundex(tempString) = end_soundex(name) LOOP
        word_count := count_words(rec.name);
        test := TRUE;
        tempString := get_last_words(fullStreet, word_count);
        FOR i IN 1..word_count LOOP
          IF soundex(split_part(tempString, ' ', i)) !=
            soundex(split_part(rec.name, ' ', i)) THEN
            test := FALSE;
          END IF;
        END LOOP;
          IF test THEN
            -- The soundex matched, determine if the distance is better.
            IF levenshtein_ignore_case(rec.name, tempString) < tempInt THEN
              location := tempString;
              tempInt := levenshtein_ignore_case(rec.name, tempString);
            END IF;
          END IF;
      END LOOP;
    ELSE
      FOR rec IN SELECT name FROM place
          WHERE soundex(tempString) = end_soundex(name) LOOP
        word_count := count_words(rec.name);
        test := TRUE;
        tempString := get_last_words(fullStreet, word_count);
        FOR i IN 1..word_count LOOP
          IF soundex(split_part(tempString, ' ', i)) !=
            soundex(split_part(rec.name, ' ', i)) THEN
            test := FALSE;
          END IF;
        END LOOP;
          IF test THEN
            -- The soundex matched, determine if the distance is better.
            IF levenshtein_ignore_case(rec.name, tempString) < tempInt THEN
              location := tempString;
            tempInt := levenshtein_ignore_case(rec.name, tempString);
          END IF;
        END IF;
      END LOOP;
    END IF;
  END IF;

  RETURN location;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.missing_indexes_generate_script()
 RETURNS text
 LANGUAGE sql
AS $function$
SELECT array_to_string(ARRAY(
-- create unique index on faces for tfid seems to perform better --
SELECT 'CREATE UNIQUE INDEX uidx_' || c.table_schema || '_' || c.table_name || '_' || c.column_name || ' ON ' || c.table_schema || '.' || c.table_name || ' USING btree(' || c.column_name || ');' As index
FROM (SELECT table_name, table_schema  FROM
	information_schema.tables WHERE table_type = 'BASE TABLE') As t  INNER JOIN
	(SELECT * FROM information_schema.columns WHERE column_name IN('tfid') ) AS c
		ON (t.table_name = c.table_name AND t.table_schema = c.table_schema)
		LEFT JOIN pg_catalog.pg_indexes i ON
			(i.tablename = c.table_name AND i.schemaname = c.table_schema
				AND  indexname LIKE 'uidx%' || c.column_name || '%' )
WHERE i.tablename IS NULL AND c.table_schema IN('tiger','tiger_data') AND c.table_name LIKE '%faces'
UNION ALL
-- basic btree regular indexes
SELECT 'CREATE INDEX idx_' || c.table_schema || '_' || c.table_name || '_' || c.column_name || ' ON ' || c.table_schema || '.' || c.table_name || ' USING btree(' || c.column_name || ');' As index
FROM (SELECT table_name, table_schema  FROM
	information_schema.tables WHERE table_type = 'BASE TABLE') As t  INNER JOIN
	(SELECT * FROM information_schema.columns WHERE column_name IN('countyfp', 'tlid', 'tfidl', 'tfidr', 'tfid', 'zip', 'placefp', 'cousubfp') ) AS c
		ON (t.table_name = c.table_name AND t.table_schema = c.table_schema)
		LEFT JOIN pg_catalog.pg_indexes i ON
			(i.tablename = c.table_name AND i.schemaname = c.table_schema
				AND  indexdef LIKE '%' || c.column_name || '%' )
WHERE i.tablename IS NULL AND c.table_schema IN('tiger','tiger_data')  AND (NOT c.table_name LIKE '%faces')
-- Gist spatial indexes --
UNION ALL
SELECT 'CREATE INDEX idx_' || c.table_schema || '_' || c.table_name || '_' || c.column_name || '_gist ON ' || c.table_schema || '.' || c.table_name || ' USING gist(' || c.column_name || ');' As index
FROM (SELECT table_name, table_schema FROM
	information_schema.tables WHERE table_type = 'BASE TABLE') As t  INNER JOIN
	(SELECT * FROM information_schema.columns WHERE column_name IN('the_geom', 'geom') ) AS c
		ON (t.table_name = c.table_name AND t.table_schema = c.table_schema)
		LEFT JOIN pg_catalog.pg_indexes i ON
			(i.tablename = c.table_name AND i.schemaname = c.table_schema
				AND  indexdef LIKE '%' || c.column_name || '%')
WHERE i.tablename IS NULL AND c.table_schema IN('tiger','tiger_data')
-- Soundex indexes --
UNION ALL
SELECT 'CREATE INDEX idx_' || c.table_schema || '_' || c.table_name || '_snd_' || c.column_name || ' ON ' || c.table_schema || '.' || c.table_name || ' USING btree(soundex(' || c.column_name || '));' As index
FROM (SELECT table_name, table_schema FROM
	information_schema.tables WHERE table_type = 'BASE TABLE') As t  INNER JOIN
	(SELECT * FROM information_schema.columns WHERE column_name IN('name', 'place', 'city') ) AS c
		ON (t.table_name = c.table_name AND t.table_schema = c.table_schema)
		LEFT JOIN pg_catalog.pg_indexes i ON
			(i.tablename = c.table_name AND i.schemaname = c.table_schema
				AND  indexdef LIKE '%soundex(%' || c.column_name || '%' AND indexdef LIKE '%_snd_' || c.column_name || '%' )
WHERE i.tablename IS NULL AND c.table_schema IN('tiger','tiger_data')
    AND (c.table_name LIKE '%county%' OR c.table_name LIKE '%featnames'
    OR c.table_name  LIKE '%place' or c.table_name LIKE '%zip%'  or c.table_name LIKE '%cousub')
-- Lower indexes --
UNION ALL
SELECT 'CREATE INDEX idx_' || c.table_schema || '_' || c.table_name || '_lower_' || c.column_name || ' ON ' || c.table_schema || '.' || c.table_name || ' USING btree(lower(' || c.column_name || '));' As index
FROM (SELECT table_name, table_schema FROM
	information_schema.tables WHERE table_type = 'BASE TABLE') As t  INNER JOIN
	(SELECT * FROM information_schema.columns WHERE column_name IN('name', 'place', 'city') ) AS c
		ON (t.table_name = c.table_name AND t.table_schema = c.table_schema)
		LEFT JOIN pg_catalog.pg_indexes i ON
			(i.tablename = c.table_name AND i.schemaname = c.table_schema
				AND  indexdef LIKE '%btree%(%lower(%' || c.column_name || '%')
WHERE i.tablename IS NULL AND c.table_schema IN('tiger','tiger_data')
    AND (c.table_name LIKE '%county%' OR c.table_name LIKE '%featnames' OR c.table_name  LIKE '%place' or c.table_name LIKE '%zip%' or c.table_name LIKE '%cousub')
-- Least address index btree least_hn(fromhn, tohn)
UNION ALL
SELECT 'CREATE INDEX idx_' || c.table_schema || '_' || c.table_name || '_least_address' || ' ON ' || c.table_schema || '.' || c.table_name || ' USING btree(least_hn(fromhn, tohn));' As index
FROM (SELECT table_name, table_schema FROM
	information_schema.tables WHERE table_type = 'BASE TABLE' AND table_name LIKE '%addr' AND table_schema IN('tiger','tiger_data')) As t  INNER JOIN
	(SELECT * FROM information_schema.columns WHERE column_name IN('fromhn') ) AS c
		ON (t.table_name = c.table_name AND t.table_schema = c.table_schema)
		LEFT JOIN pg_catalog.pg_indexes i ON
			(i.tablename = c.table_name AND i.schemaname = c.table_schema
				AND  indexdef LIKE '%least_hn(%' || c.column_name || '%')
WHERE i.tablename IS NULL
-- var_ops lower --
UNION ALL
SELECT 'CREATE INDEX idx_' || c.table_schema || '_' || c.table_name || '_l' || c.column_name || '_var_ops' || ' ON ' || c.table_schema || '.' || c.table_name || ' USING btree(lower(' || c.column_name || ') varchar_pattern_ops);' As index
FROM (SELECT table_name, table_schema FROM
	information_schema.tables WHERE table_type = 'BASE TABLE' AND (table_name LIKE '%featnames' or table_name LIKE '%place' or table_name LIKE '%zip_lookup_base' or table_name LIKE '%zip_state_loc') AND table_schema IN('tiger','tiger_data')) As t  INNER JOIN
	(SELECT * FROM information_schema.columns WHERE column_name IN('name', 'city', 'place', 'fullname') ) AS c
		ON (t.table_name = c.table_name AND t.table_schema = c.table_schema)
		LEFT JOIN pg_catalog.pg_indexes i ON
			(i.tablename = c.table_name AND i.schemaname = c.table_schema
				AND  indexdef LIKE '%btree%(%lower%' || c.column_name || ')%varchar_pattern_ops%')
WHERE i.tablename IS NULL
-- var_ops mtfcc --
/** UNION ALL
SELECT 'CREATE INDEX idx_' || c.table_schema || '_' || c.table_name || '_' || c.column_name || '_var_ops' || ' ON ' || c.table_schema || '.' || c.table_name || ' USING btree(' || c.column_name || ' varchar_pattern_ops);' As index
FROM (SELECT table_name, table_schema FROM
	information_schema.tables WHERE table_type = 'BASE TABLE' AND (table_name LIKE '%featnames' or table_name LIKE '%edges') AND table_schema IN('tiger','tiger_data')) As t  INNER JOIN
	(SELECT * FROM information_schema.columns WHERE column_name IN('mtfcc') ) AS c
		ON (t.table_name = c.table_name AND t.table_schema = c.table_schema)
		LEFT JOIN pg_catalog.pg_indexes i ON
			(i.tablename = c.table_name AND i.schemaname = c.table_schema
				AND  indexdef LIKE '%btree%(' || c.column_name || '%varchar_pattern_ops%')
WHERE i.tablename IS NULL **/
-- zipl zipr on edges --
UNION ALL
SELECT 'CREATE INDEX idx_' || c.table_schema || '_' || c.table_name || '_' || c.column_name || ' ON ' || c.table_schema || '.' || c.table_name || ' USING btree(' || c.column_name || ' );' As index
FROM (SELECT table_name, table_schema FROM
	information_schema.tables WHERE table_type = 'BASE TABLE' AND table_name LIKE '%edges' AND table_schema IN('tiger','tiger_data')) As t  INNER JOIN
	(SELECT * FROM information_schema.columns WHERE column_name IN('zipl', 'zipr') ) AS c
		ON (t.table_name = c.table_name AND t.table_schema = c.table_schema)
		LEFT JOIN pg_catalog.pg_indexes i ON
			(i.tablename = c.table_name AND i.schemaname = c.table_schema
				AND  indexdef LIKE '%btree%(' || c.column_name || '%)%')
WHERE i.tablename IS NULL

-- unique index on tlid state county --
/*UNION ALL
SELECT 'CREATE UNIQUE INDEX uidx_' || t.table_schema || '_' || t.table_name || '_tlid_statefp_countyfp ON ' || t.table_schema || '.' || t.table_name || ' USING btree(tlid,statefp,countyfp);' As index
FROM (SELECT table_name, table_schema FROM
	information_schema.tables WHERE table_type = 'BASE TABLE' AND table_name LIKE '%edges' AND table_schema IN('tiger','tiger_data')) As t
		LEFT JOIN pg_catalog.pg_indexes i ON
			(i.tablename = t.table_name AND i.schemaname = t.table_schema
				AND  indexdef LIKE '%btree%(%tlid,%statefp%countyfp%)%')
WHERE i.tablename IS NULL*/
--full text indexes on name field--
/**UNION ALL
SELECT 'CREATE INDEX idx_' || c.table_schema || '_' || c.table_name || '_fullname_ft_gist' || ' ON ' || c.table_schema || '.' || c.table_name || ' USING gist(to_tsvector(''english'',fullname))' As index
FROM (SELECT table_name, table_schema FROM
	information_schema.tables WHERE table_type = 'BASE TABLE' AND table_name LIKE '%featnames' AND table_schema IN('tiger','tiger_data')) As t  INNER JOIN
	(SELECT * FROM information_schema.columns WHERE column_name IN('fullname') ) AS c
		ON (t.table_name = c.table_name AND t.table_schema = c.table_schema)
		LEFT JOIN pg_catalog.pg_indexes i ON
			(i.tablename = c.table_name AND i.schemaname = c.table_schema
				AND  indexdef LIKE '%to_tsvector(%' || c.column_name || '%')
WHERE i.tablename IS NULL **/

-- trigram index --
/**UNION ALL
SELECT 'CREATE INDEX idx_' || c.table_schema || '_' || c.table_name || '_' || c.column_name || '_trgm_gist' || ' ON ' || c.table_schema || '.' || c.table_name || ' USING gist(' || c.column_name || ' gist_trgm_ops);' As index
FROM (SELECT table_name, table_schema FROM
	information_schema.tables WHERE table_type = 'BASE TABLE' AND table_name LIKE '%featnames' AND table_schema IN('tiger','tiger_data')) As t  INNER JOIN
	(SELECT * FROM information_schema.columns WHERE column_name IN('fullname', 'name') ) AS c
		ON (t.table_name = c.table_name AND t.table_schema = c.table_schema)
		LEFT JOIN pg_catalog.pg_indexes i ON
			(i.tablename = c.table_name AND i.schemaname = c.table_schema
				AND  indexdef LIKE '%gist%(' || c.column_name || '%gist_trgm_ops%')
WHERE i.tablename IS NULL **/
ORDER BY 1), E'\r');
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.normalize_address(in_rawinput character varying)
 RETURNS norm_addy
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
DECLARE
  debug_flag boolean := get_geocode_setting('debug_normalize_address')::boolean;
  use_pagc boolean := COALESCE(get_geocode_setting('use_pagc_address_parser')::boolean, false);
  result norm_addy;
  addressString VARCHAR;
  zipString VARCHAR;
  preDir VARCHAR;
  postDir VARCHAR;
  fullStreet VARCHAR;
  reducedStreet VARCHAR;
  streetType VARCHAR;
  state VARCHAR;
  tempString VARCHAR;
  tempInt INTEGER;
  rec RECORD;
  ws VARCHAR;
  rawInput VARCHAR;
  -- is this a highway
  -- (we treat these differently since the road name often comes after the streetType)
  isHighway boolean := false;
BEGIN
  result.parsed := FALSE;
  IF use_pagc THEN
  	result := pagc_normalize_address(in_rawinput);
  	RETURN result;
  END IF;

  rawInput := trim(in_rawInput);

  IF rawInput IS NULL THEN
    RETURN result;
  END IF;

  ws := E'[ ,.\t\n\f\r]';

  IF debug_flag THEN
    raise notice '% input: %', clock_timestamp(), rawInput;
  END IF;

  -- Assume that the address begins with a digit, and extract it from
  -- the input string.
  addressString := substring(rawInput from E'^([0-9].*?)[ ,/.]');

  -- try to pull full street number including non-digits like 1R
  result.address_alphanumeric := substring(rawInput from E'^([0-9a-zA-Z].*?)[ ,/.]');

  IF debug_flag THEN
    raise notice '% addressString: %', clock_timestamp(), addressString;
  END IF;

  -- There are two formats for zip code, the normal 5 digit , and
  -- the nine digit zip-4.  It may also not exist.

  zipString := substring(rawInput from ws || E'([0-9]{5})$');
  IF zipString IS NULL THEN
    -- Check if the zip is just a partial or a one with -s
    -- or one that just has more than 5 digits
    zipString := COALESCE(substring(rawInput from ws || '([0-9]{5})-[0-9]{0,4}$'),
                substring(rawInput from ws || '([0-9]{2,5})$'),
                substring(rawInput from ws || '([0-9]{6,14})$'));

    result.zip4 := COALESCE(substring(rawInput from ws || '[0-9]{5}-([0-9]{0,4})$'),substring(rawInput from ws || '[0-9]{5}([0-9]{0,4})$'));

    IF debug_flag THEN
        raise notice '% zip4: %', clock_timestamp(), result.zip4;
    END IF;
     -- Check if all we got was a zipcode, of either form
    IF zipString IS NULL THEN
      zipString := substring(rawInput from '^([0-9]{5})$');
      IF zipString IS NULL THEN
        zipString := substring(rawInput from '^([0-9]{5})-[0-9]{4}$');
      END IF;
      -- If it was only a zipcode, then just return it.
      IF zipString IS NOT NULL THEN
        result.zip := zipString;
        result.parsed := TRUE;
        RETURN result;
      END IF;
    END IF;
  END IF;

  IF debug_flag THEN
    raise notice '% zipString: %', clock_timestamp(), zipString;
  END IF;

  IF zipString IS NOT NULL THEN
    fullStreet := substring(rawInput from '(.*)'
        || ws || '+' || cull_null(zipString) || '[- ]?([0-9]{4})?$');
    /** strip off any trailing  spaces or ,**/
    fullStreet :=  btrim(fullStreet, ' ,');

  ELSE
    fullStreet := rawInput;
  END IF;

  IF debug_flag THEN
    raise notice '% fullStreet: %', clock_timestamp(), fullStreet;
  END IF;

  -- FIXME: state_extract should probably be returning a record so we can
  -- avoid having to parse the result from it.
  tempString := state_extract(fullStreet);
  IF tempString IS NOT NULL THEN
    state := split_part(tempString, ':', 1);
    result.stateAbbrev := split_part(tempString, ':', 2);
  END IF;

  IF debug_flag THEN
    raise notice '% stateAbbrev: %', clock_timestamp(), result.stateAbbrev;
  END IF;

  -- The easiest case is if the address is comma delimited.  There are some
  -- likely cases:
  --   street level, location, state
  --   street level, location state
  --   street level, location
  --   street level, internal address, location, state
  --   street level, internal address, location state
  --   street level, internal address location state
  --   street level, internal address, location
  --   street level, internal address location
  -- The first three are useful.

  tempString := substring(fullStreet, '(?i),' || ws || '+(.*?)(,?' || ws ||
      '*' || cull_null(state) || '$)');
  IF tempString = '' THEN tempString := NULL; END IF;
  IF tempString IS NOT NULL THEN
    IF tempString LIKE '%,%' THEN -- if it has a comma probably has suite, strip it from location
        result.location := trim(split_part(tempString,',',2));
    ELSE
        result.location := tempString;
    END IF;
    IF addressString IS NOT NULL THEN
      fullStreet := substring(fullStreet, '(?i)' || addressString || ws ||
          '+(.*),' || ws || '+' || result.location);
    ELSE
      fullStreet := substring(fullStreet, '(?i)(.*),' || ws || '+' ||
          result.location);
    END IF;
  END IF;

  IF debug_flag THEN
    raise notice '% fullStreet: %',  clock_timestamp(), fullStreet;
    raise notice '% location: %', clock_timestamp(), result.location;
  END IF;

  -- Pull out the full street information, defined as everything between the
  -- address and the state.  This includes the location.
  -- This doesn't need to be done if location has already been found.
  IF result.location IS NULL THEN
    IF addressString IS NOT NULL THEN
      IF state IS NOT NULL THEN
        fullStreet := substring(fullStreet, '(?i)' || addressString ||
            ws || '+(.*?)' || ws || '+' || state);
      ELSE
        fullStreet := substring(fullStreet, '(?i)' || addressString ||
            ws || '+(.*?)');
      END IF;
    ELSE
      IF state IS NOT NULL THEN
        fullStreet := substring(fullStreet, '(?i)(.*?)' || ws ||
            '+' || state);
      ELSE
        fullStreet := substring(fullStreet, '(?i)(.*?)');
      END IF;
    END IF;

    IF debug_flag THEN
      raise notice '% fullStreet: %', clock_timestamp(),fullStreet;
    END IF;

    IF debug_flag THEN
      raise notice '% start location extract', clock_timestamp();
    END IF;
    result.location := location_extract(fullStreet, result.stateAbbrev);

    IF debug_flag THEN
      raise notice '% end location extract', clock_timestamp();
    END IF;

    -- A location can't be a street type, sorry.
    IF lower(result.location) IN (SELECT lower(name) FROM street_type_lookup) THEN
        result.location := NULL;
    END IF;

    -- If the location was found, remove it from fullStreet
    IF result.location IS NOT NULL THEN
      fullStreet := substring(fullStreet, '(?i)(.*)' || ws || '+' ||
          result.location);
    END IF;
  END IF;

  IF debug_flag THEN
    raise notice 'fullStreet: %', fullStreet;
    raise notice 'location: %', result.location;
  END IF;

  -- Determine if any internal address is included, such as apartment
  -- or suite number.
  -- this count is surprisingly slow by itself but much faster if you add an ILIKE AND clause
  SELECT INTO tempInt count(*) FROM secondary_unit_lookup
      WHERE fullStreet ILIKE '%' || name || '%' AND texticregexeq(fullStreet, '(?i)' || ws || name || '('
          || ws || '|$)');
  IF tempInt = 1 THEN
    result.internal := substring(fullStreet, '(?i)' || ws || '('
        || name ||  ws || '*#?' || ws
        || '*(?:[0-9][-0-9a-zA-Z]*)?' || ')(?:' || ws || '|$)')
        FROM secondary_unit_lookup
        WHERE fullStreet ILIKE '%' || name || '%' AND texticregexeq(fullStreet, '(?i)' || ws || name || '('
        || ws || '|$)');
    ELSIF tempInt > 1 THEN
    -- In the event of multiple matches to a secondary unit designation, we
    -- will assume that the last one is the true one.
    tempInt := 0;
    FOR rec in SELECT trim(substring(fullStreet, '(?i)' || ws || '('
        || name || '(?:' || ws || '*#?' || ws
        || '*(?:[0-9][-0-9a-zA-Z]*)?)' || ws || '?|$)')) as value
        FROM secondary_unit_lookup
        WHERE fullStreet ILIKE '%' || name || '%' AND  texticregexeq(fullStreet, '(?i)' || ws || name || '('
        || ws || '|$)') LOOP
      IF tempInt < position(rec.value in fullStreet) THEN
        tempInt := position(rec.value in fullStreet);
        result.internal := rec.value;
      END IF;
    END LOOP;
  END IF;

  IF debug_flag THEN
    raise notice 'internal: %', result.internal;
  END IF;

  IF result.location IS NULL THEN
    -- If the internal address is given, the location is everything after it.
    result.location := trim(substring(fullStreet, result.internal || ws || '+(.*)$'));
  END IF;

  IF debug_flag THEN
    raise notice 'location: %', result.location;
  END IF;

  -- Pull potential street types from the full street information
  -- this count is surprisingly slow by itself but much faster if you add an ILIKE AND clause
  -- difference of 98ms vs 16 ms for example
  -- Put a space in front to make regex easier can always count on it starting with space
  -- Reject all street types where the fullstreet name is equal to the name
  fullStreet := ' ' || trim(fullStreet);
  tempInt := count(*) FROM street_type_lookup
      WHERE fullStreet ILIKE '%' || name || '%' AND
        trim(upper(fullStreet)) != name AND
        texticregexeq(fullStreet, '(?i)' || ws || '(' || name
      || ')(?:' || ws || '|$)');
  IF tempInt = 1 THEN
    SELECT INTO rec abbrev, substring(fullStreet, '(?i)' || ws || '('
        || name || ')(?:' || ws || '|$)') AS given, is_hw FROM street_type_lookup
        WHERE fullStreet ILIKE '%' || name || '%' AND
             trim(upper(fullStreet)) != name AND
            texticregexeq(fullStreet, '(?i)' || ws || '(' || name
        || ')(?:' || ws || '|$)')  ;
    streetType := rec.given;
    result.streetTypeAbbrev := rec.abbrev;
    isHighway :=  rec.is_hw;
    IF debug_flag THEN
    	   RAISE NOTICE 'street Type: %, street Type abbrev: %', rec.given, rec.abbrev;
    END IF;
  ELSIF tempInt > 1 THEN
    tempInt := 0;
    -- the last matching abbrev in the string is the most likely one
    FOR rec IN SELECT * FROM
    	(SELECT abbrev, name, substring(fullStreet, '(?i)' || ws || '?('
        || name || ')(?:' || ws || '|$)') AS given, is_hw ,
        		RANK() OVER( ORDER BY position(name IN upper(trim(fullStreet))) ) As n_start,
        		RANK() OVER( ORDER BY position(name IN upper(trim(fullStreet))) + length(name) ) As n_end,
        		COUNT(*) OVER() As nrecs, position(name IN upper(trim(fullStreet)))
        		FROM street_type_lookup
        WHERE fullStreet ILIKE '%' || name || '%'  AND
            trim(upper(fullStreet)) != name AND
            (texticregexeq(fullStreet, '(?i)' || ws || '(' || name
            -- we only consider street types that are regular and not at beginning of name or are highways (since those can be at beg or end)
            -- we take the one that is the longest e.g Country Road would be more correct than Road
        || ')(?:' || ws || '|$)') OR (is_hw AND fullstreet ILIKE name || ' %') )
     AND ((NOT is_hw AND position(name IN upper(trim(fullStreet))) > 1 OR is_hw) )
        ) As foo
        -- N_start - N_end - ensure we first get the one with the most overlapping sub types
        -- Then of those get the one that ends last and then starts first
        ORDER BY n_start - n_end, n_end DESC, n_start LIMIT 1  LOOP
      -- If we have found an internal address, make sure the type
      -- precedes it.
      /** TODO: I don't think we need a loop anymore since we are just returning one and the one in the last position
      * I'll leave for now though **/
      IF result.internal IS NOT NULL THEN
        IF position(rec.given IN fullStreet) < position(result.internal IN fullStreet) THEN
          IF tempInt < position(rec.given IN fullStreet) THEN
            streetType := rec.given;
            result.streetTypeAbbrev := rec.abbrev;
            isHighway := rec.is_hw;
            tempInt := position(rec.given IN fullStreet);
          END IF;
        END IF;
      ELSIF tempInt < position(rec.given IN fullStreet) THEN
        streetType := rec.given;
        result.streetTypeAbbrev := rec.abbrev;
        isHighway := rec.is_hw;
        tempInt := position(rec.given IN fullStreet);
        IF debug_flag THEN
        	RAISE NOTICE 'street Type: %, street Type abbrev: %', rec.given, rec.abbrev;
        END IF;
      END IF;
    END LOOP;
  END IF;

  IF debug_flag THEN
    raise notice '% streetTypeAbbrev: %', clock_timestamp(), result.streetTypeAbbrev;
  END IF;

  -- There is a little more processing required now.  If the word after the
  -- street type begins with a number, then its most likely a highway like State Route 225a.  If
  -- In Tiger 2010+ the reduced Street name just has the number
  -- the next word starts with a char, then everything after the street type
  -- will be considered location.  If there is no street type, then I'm sad.
  IF streetType IS NOT NULL THEN
    -- Check if the fullStreet contains the streetType and ends in just numbers
    -- If it does its a road number like a country road or state route or other highway
    -- Just set the number to be the name of street

    tempString := NULL;
    IF isHighway THEN
        tempString :=  substring(fullStreet, streetType || ws || '+' || E'([0-9a-zA-Z]+)' || ws || '*');
    END IF;
    IF tempString > '' AND result.location IS NOT NULL THEN
        reducedStreet := tempString;
        result.streetName := reducedStreet;
        IF debug_flag THEN
        	RAISE NOTICE 'reduced Street: %', result.streetName;
        END IF;
        -- the post direction might be portion of fullStreet after reducedStreet and type
		-- reducedStreet: 24  fullStreet: Country Road 24, N or fullStreet: Country Road 24 N
		tempString := regexp_replace(fullStreet, streetType || ws || '+' || reducedStreet,'');
		IF tempString > '' THEN
			IF debug_flag THEN
				RAISE NOTICE 'remove reduced street: % + streetType: % from fullstreet: %', reducedStreet, streetType, fullStreet;
			END IF;
			tempString := abbrev FROM direction_lookup WHERE
			 tempString ILIKE '%' || name || '%'  AND texticregexeq(reducedStreet || ws || '+' || streetType, '(?i)(' || name || ')' || ws || '+|$')
			 	ORDER BY length(name) DESC LIMIT 1;
			IF tempString IS NOT NULL THEN
				result.postDirAbbrev = trim(tempString);
				IF debug_flag THEN
					RAISE NOTICE 'postDirAbbre of highway: %', result.postDirAbbrev;
				END IF;
			END IF;
		END IF;
    ELSE
        tempString := substring(fullStreet, streetType || ws ||
            E'+([0-9][^ ,.\t\r\n\f]*?)' || ws);
        IF tempString IS NOT NULL THEN
          IF result.location IS NULL THEN
            result.location := substring(fullStreet, streetType || ws || '+'
                     || tempString || ws || '+(.*)$');
          END IF;
          reducedStreet := substring(fullStreet, '(.*)' || ws || '+'
                        || result.location || '$');
          streetType := NULL;
          result.streetTypeAbbrev := NULL;
        ELSE
          IF result.location IS NULL THEN
            result.location := substring(fullStreet, streetType || ws || '+(.*)$');
          END IF;
          reducedStreet := substring(fullStreet, '^(.*)' || ws || '+'
                        || streetType);
          IF COALESCE(trim(reducedStreet),'') = '' THEN --reduced street can't be blank
            reducedStreet := fullStreet;
            streetType := NULL;
            result.streetTypeAbbrev := NULL;
          END IF;
        END IF;
		-- the post direction might be portion of fullStreet after reducedStreet
		-- reducedStreet: Main  fullStreet: Main St, N or fullStreet: Main St N
		tempString := trim(regexp_replace(fullStreet,  reducedStreet ||  ws || '+' || streetType,''));
		IF tempString > '' THEN
		  tempString := abbrev FROM direction_lookup WHERE
			 tempString ILIKE '%' || name || '%'
			 AND texticregexeq(fullStreet || ' ', '(?i)' || reducedStreet || ws || '+' || streetType || ws || '+(' || name || ')' || ws || '+')
			ORDER BY length(name) DESC LIMIT 1;
		  IF tempString IS NOT NULL THEN
			result.postDirAbbrev = trim(tempString);
		  END IF;
		END IF;

		IF debug_flag THEN
			raise notice '% reduced street: %', clock_timestamp(), reducedStreet;
		END IF;

		-- The pre direction should be at the beginning of the fullStreet string.
		-- The post direction should be at the beginning of the location string
		-- if there is no internal address
		reducedStreet := trim(reducedStreet);
		tempString := trim(regexp_replace(fullStreet,  ws || '+' || reducedStreet ||  ws || '+',''));
		IF tempString > '' THEN
			tempString := substring(reducedStreet, '(?i)(^' || name
				|| ')' || ws) FROM direction_lookup WHERE
				 reducedStreet ILIKE '%' || name || '%'  AND texticregexeq(reducedStreet, '(?i)(^' || name || ')' || ws)
				ORDER BY length(name) DESC LIMIT 1;
		END IF;
		IF tempString > '' THEN
		  preDir := tempString;
		  result.preDirAbbrev := abbrev FROM direction_lookup
			  where reducedStreet ILIKE '%' || name '%' AND texticregexeq(reducedStreet, '(?i)(^' || name || ')' || ws)
			  ORDER BY length(name) DESC LIMIT 1;
		  result.streetName := trim(substring(reducedStreet, '^' || preDir || ws || '(.*)'));
		ELSE
		  result.streetName := trim(reducedStreet);
		END IF;
    END IF;
    IF texticregexeq(result.location, '(?i)' || result.internal || '$') THEN
      -- If the internal address is at the end of the location, then no
      -- location was given.  We still need to look for post direction.
      SELECT INTO rec abbrev,
          substring(result.location, '(?i)^(' || name || ')' || ws) as value
          FROM direction_lookup
            WHERE result.location ILIKE '%' || name || '%' AND texticregexeq(result.location, '(?i)^'
          || name || ws) ORDER BY length(name) desc LIMIT 1;
      IF rec.value IS NOT NULL THEN
        postDir := rec.value;
        result.postDirAbbrev := rec.abbrev;
      END IF;
      result.location := null;
    ELSIF result.internal IS NULL THEN
      -- If no location is given, the location string will be the post direction
      SELECT INTO tempInt count(*) FROM direction_lookup WHERE
          upper(result.location) = upper(name);
      IF tempInt != 0 THEN
        postDir := result.location;
        SELECT INTO result.postDirAbbrev abbrev FROM direction_lookup WHERE
            upper(postDir) = upper(name);
        result.location := NULL;

        IF debug_flag THEN
            RAISE NOTICE '% postDir exact match: %', clock_timestamp(), result.postDirAbbrev;
        END IF;
      ELSE
        -- postDirection is not equal location, but may be contained in it
        -- It is only considered a postDirection if it is not preceded by a ,
        SELECT INTO tempString substring(result.location, '(?i)(^' || name
            || ')' || ws) FROM direction_lookup WHERE
            result.location ILIKE '%' || name || '%' AND texticregexeq(result.location, '(?i)(^' || name || ')' || ws)
            	AND NOT  texticregexeq(rawInput, '(?i)(,' || ws || '+' || result.location || ')' || ws)
            ORDER BY length(name) desc LIMIT 1;

        IF debug_flag THEN
            RAISE NOTICE '% location trying to extract postdir: %, tempstring: %, rawInput: %', clock_timestamp(), result.location, tempString, rawInput;
        END IF;
        IF tempString IS NOT NULL THEN
            postDir := tempString;
            SELECT INTO result.postDirAbbrev abbrev FROM direction_lookup
              WHERE result.location ILIKE '%' || name || '%' AND texticregexeq(result.location, '(?i)(^' || name || ')' || ws) ORDER BY length(name) DESC LIMIT 1;
              result.location := substring(result.location, '^' || postDir || ws || '+(.*)');
            IF debug_flag THEN
                  RAISE NOTICE '% postDir: %', clock_timestamp(), result.postDirAbbrev;
            END IF;
        END IF;

      END IF;
    ELSE
      -- internal is not null, but is not at the end of the location string
      -- look for post direction before the internal address
        IF debug_flag THEN
            RAISE NOTICE '%fullstreet before extract postdir: %', clock_timestamp(), fullStreet;
        END IF;
        SELECT INTO tempString substring(fullStreet, '(?i)' || streetType
          || ws || '+(' || name || ')' || ws || '+' || result.internal)
          FROM direction_lookup
          WHERE fullStreet ILIKE '%' || name || '%' AND texticregexeq(fullStreet, '(?i)'
          || ws || name || ws || '+' || result.internal) ORDER BY length(name) desc LIMIT 1;
        IF tempString IS NOT NULL THEN
            postDir := tempString;
            SELECT INTO result.postDirAbbrev abbrev FROM direction_lookup
                WHERE texticregexeq(fullStreet, '(?i)' || ws || name || ws);
        END IF;
    END IF;
  ELSE
  -- No street type was found

    -- If an internal address was given, then the split becomes easy, and the
    -- street name is everything before it, without directions.
    IF result.internal IS NOT NULL THEN
      reducedStreet := substring(fullStreet, '(?i)^(.*?)' || ws || '+'
                    || result.internal);
      tempInt := count(*) FROM direction_lookup WHERE
          reducedStreet ILIKE '%' || name || '%' AND texticregexeq(reducedStreet, '(?i)' || ws || name || '$');
      IF tempInt > 0 THEN
        postDir := substring(reducedStreet, '(?i)' || ws || '('
            || name || ')' || '$') FROM direction_lookup
            WHERE reducedStreet ILIKE '%' || name || '%' AND texticregexeq(reducedStreet, '(?i)' || ws || name || '$');
        result.postDirAbbrev := abbrev FROM direction_lookup
            WHERE texticregexeq(reducedStreet, '(?i)' || ws || name || '$');
      END IF;
      tempString := substring(reducedStreet, '(?i)^(' || name
          || ')' || ws) FROM direction_lookup WHERE
           reducedStreet ILIKE '%' || name || '%' AND texticregexeq(reducedStreet, '(?i)^(' || name || ')' || ws)
          ORDER BY length(name) DESC;
      IF tempString IS NOT NULL THEN
        preDir := tempString;
        result.preDirAbbrev := abbrev FROM direction_lookup WHERE
             reducedStreet ILIKE '%' || name || '%' AND texticregexeq(reducedStreet, '(?i)(^' || name || ')' || ws)
            ORDER BY length(name) DESC;
        result.streetName := substring(reducedStreet, '(?i)^' || preDir || ws
                   || '+(.*?)(?:' || ws || '+' || cull_null(postDir) || '|$)');
      ELSE
        result.streetName := substring(reducedStreet, '(?i)^(.*?)(?:' || ws
                   || '+' || cull_null(postDir) || '|$)');
      END IF;
    ELSE

      -- If a post direction is given, then the location is everything after,
      -- the street name is everything before, less any pre direction.
      fullStreet := trim(fullStreet);
      tempInt := count(*) FROM direction_lookup
          WHERE fullStreet ILIKE '%' || name || '%' AND texticregexeq(fullStreet, '(?i)' || ws || name || '(?:'
              || ws || '|$)');

      IF tempInt = 1 THEN
        -- A single postDir candidate was found.  This makes it easier.
        postDir := substring(fullStreet, '(?i)' || ws || '('
            || name || ')(?:' || ws || '|$)') FROM direction_lookup WHERE
             fullStreet ILIKE '%' || name || '%' AND texticregexeq(fullStreet, '(?i)' || ws || name || '(?:'
            || ws || '|$)');
        result.postDirAbbrev := abbrev FROM direction_lookup
            WHERE fullStreet ILIKE '%' || name || '%' AND texticregexeq(fullStreet, '(?i)' || ws || name
            || '(?:' || ws || '|$)');
        IF result.location IS NULL THEN
          result.location := substring(fullStreet, '(?i)' || ws || postDir
                   || ws || '+(.*?)$');
        END IF;
        reducedStreet := substring(fullStreet, '^(.*?)' || ws || '+'
                      || postDir);
        tempString := substring(reducedStreet, '(?i)(^' || name
            || ')' || ws) FROM direction_lookup
            WHERE
                reducedStreet ILIKE '%' || name || '%' AND texticregexeq(reducedStreet, '(?i)(^' || name || ')' || ws)
            ORDER BY length(name) DESC;
        IF tempString IS NOT NULL THEN
          preDir := tempString;
          result.preDirAbbrev := abbrev FROM direction_lookup WHERE
              reducedStreet ILIKE '%' || name || '%' AND texticregexeq(reducedStreet, '(?i)(^' || name || ')' || ws)
              ORDER BY length(name) DESC;
          result.streetName := trim(substring(reducedStreet, '^' || preDir || ws
                     || '+(.*)'));
        ELSE
          result.streetName := trim(reducedStreet);
        END IF;
      ELSIF tempInt > 1 THEN
        -- Multiple postDir candidates were found.  We need to find the last
        -- incident of a direction, but avoid getting the last word from
        -- a two word direction. eg extracting "East" from "North East"
        -- We do this by sorting by length, and taking the last direction
        -- in the results that is not included in an earlier one.
        -- This wont be a problem it preDir is North East and postDir is
        -- East as the regex requires a space before the direction.  Only
        -- the East will return from the preDir.
        tempInt := 0;
        FOR rec IN SELECT abbrev, substring(fullStreet, '(?i)' || ws || '('
            || name || ')(?:' || ws || '|$)') AS value
            FROM direction_lookup
            WHERE fullStreet ILIKE '%' || name || '%' AND texticregexeq(fullStreet, '(?i)' || ws || name
            || '(?:' || ws || '|$)')
            ORDER BY length(name) desc LOOP
          tempInt := 0;
          IF tempInt < position(rec.value in fullStreet) THEN
            IF postDir IS NULL THEN
              tempInt := position(rec.value in fullStreet);
              postDir := rec.value;
              result.postDirAbbrev := rec.abbrev;
            ELSIF NOT texticregexeq(postDir, '(?i)' || rec.value) THEN
              tempInt := position(rec.value in fullStreet);
              postDir := rec.value;
              result.postDirAbbrev := rec.abbrev;
             END IF;
          END IF;
        END LOOP;
        IF result.location IS NULL THEN
          result.location := substring(fullStreet, '(?i)' || ws || postDir || ws
                   || '+(.*?)$');
        END IF;
        reducedStreet := substring(fullStreet, '(?i)^(.*?)' || ws || '+'
                      || postDir);
        SELECT INTO tempString substring(reducedStreet, '(?i)(^' || name
            || ')' || ws) FROM direction_lookup WHERE
             reducedStreet ILIKE '%' || name || '%' AND  texticregexeq(reducedStreet, '(?i)(^' || name || ')' || ws)
            ORDER BY length(name) DESC;
        IF tempString IS NOT NULL THEN
          preDir := tempString;
          SELECT INTO result.preDirAbbrev abbrev FROM direction_lookup WHERE
              reducedStreet ILIKE '%' || name || '%' AND  texticregexeq(reducedStreet, '(?i)(^' || name || ')' || ws)
              ORDER BY length(name) DESC;
          result.streetName := substring(reducedStreet, '^' || preDir || ws
                     || '+(.*)');
        ELSE
          result.streetName := reducedStreet;
        END IF;
      ELSE

        -- There is no street type, directional suffix or internal address
        -- to allow distinction between street name and location.
        IF result.location IS NULL THEN
          IF debug_flag THEN
            raise notice 'fullStreet: %', fullStreet;
          END IF;

          result.location := location_extract(fullStreet, result.stateAbbrev);
          -- If the location was found, remove it from fullStreet
          IF result.location IS NOT NULL THEN
            fullStreet := substring(fullStreet, '(?i)(.*),' || ws || '+' ||
                result.location);
          END IF;
        END IF;

        -- Check for a direction prefix.
        SELECT INTO tempString substring(fullStreet, '(?i)(^' || name
            || ')' || ws) FROM direction_lookup WHERE
            texticregexeq(fullStreet, '(?i)(^' || name || ')' || ws)
            ORDER BY length(name);
        IF tempString IS NOT NULL THEN
          preDir := tempString;
          SELECT INTO result.preDirAbbrev abbrev FROM direction_lookup WHERE
              texticregexeq(fullStreet, '(?i)(^' || name || ')' || ws)
              ORDER BY length(name) DESC;
          IF result.location IS NOT NULL THEN
            -- The location may still be in the fullStreet, or may
            -- have been removed already
            result.streetName := substring(fullStreet, '^' || preDir || ws
                       || '+(.*?)(' || ws || '+' || result.location || '|$)');
          ELSE
            result.streetName := substring(fullStreet, '^' || preDir || ws
                       || '+(.*?)' || ws || '*');
          END IF;
        ELSE
          IF result.location IS NOT NULL THEN
            -- The location may still be in the fullStreet, or may
            -- have been removed already
            result.streetName := substring(fullStreet, '^(.*?)(' || ws
                       || '+' || result.location || '|$)');
          ELSE
            result.streetName := fullStreet;
          END IF;
        END IF;
      END IF;
    END IF;
  END IF;

 -- For address number only put numbers and stop if reach a non-number e.g. 123-456 will return 123
  result.address := to_number(substring(addressString, '[0-9]+'),  '99999999');
   --get rid of extraneous spaces before we return
  result.zip := trim(zipString);
  result.streetName := trim(result.streetName);
  result.location := trim(result.location);
  result.postDirAbbrev := trim(result.postDirAbbrev);
  result.parsed := TRUE;
  RETURN result;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.nullable_levenshtein(character varying, character varying)
 RETURNS integer
 LANGUAGE plpgsql
 IMMUTABLE COST 10
AS $function$
DECLARE
  given_string VARCHAR;
  result INTEGER := 3;
  var_verbose BOOLEAN := FALSE; /**change from verbose to param_verbose since its a keyword and get compile error in 9.0 **/
BEGIN
  IF $1 IS NULL THEN
    IF var_verbose THEN
      RAISE NOTICE 'nullable_levenshtein - given string is NULL!';
    END IF;
    RETURN NULL;
  ELSE
    given_string := $1;
  END IF;

  IF $2 IS NOT NULL AND $2 != '' THEN
    result := levenshtein_ignore_case(given_string, $2);
  END IF;

  RETURN result;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.numeric_streets_equal(input_street character varying, output_street character varying)
 RETURNS boolean
 LANGUAGE sql
 IMMUTABLE COST 5
AS $function$
    SELECT COALESCE(length($1) < 10 AND length($2) < 10
            AND $1 ~ E'^[0-9\/\s]+' AND $2 ~ E'^[0-9\/\s]+'
            AND  trim(substring($1, E'^[0-9\/\s]+')) = trim(substring($2, E'^[0-9\/\s]+')), false);
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.pagc_normalize_address(in_rawinput character varying)
 RETURNS norm_addy
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
DECLARE
  result norm_addy;
  var_rec RECORD;
  var_parse_rec RECORD;
  rawInput VARCHAR;

BEGIN
  result.parsed := FALSE;

  rawInput := trim(in_rawinput);
  var_parse_rec := parse_address(rawInput);
  result.location := var_parse_rec.city;
  result.stateAbbrev := trim(var_parse_rec.state);
  result.zip := var_parse_rec.zip;
  result.zip4 := NULLIF(var_parse_rec.zipplus,'');

 var_rec := standardize_address('pagc_lex'
       , 'pagc_gaz'
       , 'pagc_rules'
, COALESCE(var_parse_rec.address1,''),
   COALESCE(var_parse_rec.city,'') || COALESCE(', ' || var_parse_rec.state, '') || COALESCE(' ' || var_parse_rec.zip,'')  ) ;

 -- For address number only put numbers and stop if reach a non-number e.g. 123-456 will return 123
  result.address := to_number(substring(var_rec.house_num, '[0-9]+'), '99999999');
  result.address_alphanumeric := var_rec.house_num;
   --get rid of extraneous spaces before we return
  result.zip := COALESCE(var_rec.postcode,result.zip);
  result.streetName := trim(var_rec.name);
  result.location := trim(var_rec.city);
  result.stateAbbrev := trim(var_rec.state);
  --this should be broken out separately like pagc, but normalizer doesn't have a slot for it
  result.streettypeAbbrev := trim(COALESCE(var_rec.suftype, var_rec.pretype));
  result.preDirAbbrev := trim(var_rec.predir);
  result.postDirAbbrev := trim(var_rec.sufdir);
  result.internal := trim(regexp_replace(replace(var_rec.unit, '#',''), '([0-9]+)\s+([A-Za-z]){0,1}', E'\\1\\2'));
  result.parsed := TRUE;
  RETURN result;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.pprint_addy(input norm_addy)
 RETURNS character varying
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
DECLARE
  result VARCHAR;
BEGIN
  IF NOT input.parsed THEN
    RETURN NULL;
  END IF;

  result := COALESCE(input.address_alphanumeric, cull_null(input.address::text))
         || COALESCE(' ' || input.preDirAbbrev, '')
         || CASE WHEN is_pretype(input.streetTypeAbbrev) THEN ' ' || input.streetTypeAbbrev  ELSE '' END
         || COALESCE(' ' || input.streetName, '')
         || CASE WHEN NOT is_pretype(input.streetTypeAbbrev) THEN ' ' || input.streetTypeAbbrev  ELSE '' END
         || COALESCE(' ' || input.postDirAbbrev, '')
         || CASE WHEN
              input.address IS NOT NULL OR
              input.streetName IS NOT NULL
              THEN ', ' ELSE '' END
         || cull_null(input.internal)
         || CASE WHEN input.internal IS NOT NULL THEN ', ' ELSE '' END
         || cull_null(input.location)
         || CASE WHEN input.location IS NOT NULL THEN ', ' ELSE '' END
         || COALESCE(input.stateAbbrev || ' ' , '')
         || cull_null(input.zip) || COALESCE('-' || input.zip4, '');

  RETURN trim(result);

END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.rate_attributes(dirpa character varying, dirpb character varying, streetnamea character varying, streetnameb character varying, streettypea character varying, streettypeb character varying, dirsa character varying, dirsb character varying, prequalabr character varying)
 RETURNS integer
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
DECLARE
  result INTEGER := 0;
  directionWeight INTEGER := 2;
  nameWeight INTEGER := 10;
  typeWeight INTEGER := 5;
  var_verbose BOOLEAN := false;
BEGIN
  result := result + levenshtein_ignore_case(cull_null($1), cull_null($2)) * directionWeight;
  IF var_verbose THEN
    RAISE NOTICE 'streetNameA: %, streetNameB: %', streetNameA, streetNameB;
  END IF;
  IF streetNameA IS NOT NULL AND streetNameB IS NOT NULL THEN
    -- We want to treat numeric streets that have numerics as equal
    -- and not penalize if they are spelled different e.g. have ND instead of TH
    IF NOT numeric_streets_equal(streetNameA, streetNameB) THEN
        IF prequalabr IS NOT NULL THEN
            -- If the reference address (streetNameB) has a prequalabr streetNameA (prequalabr) - note: streetNameB usually comes thru without prequalabr
            -- and the input street (streetNameA) is lacking the prequal -- only penalize a little
            result := (result + levenshtein_ignore_case( trim( trim( lower(streetNameA),lower(prequalabr) ) ), trim( trim( lower(streetNameB),lower(prequalabr) ) ) )*nameWeight*0.75 + levenshtein_ignore_case(trim(streetNameA),prequalabr || ' ' ||  streetNameB) * nameWeight*0.25)::integer;
        ELSE
            result := result + levenshtein_ignore_case(streetNameA, streetNameB) * nameWeight;
        END IF;
    ELSE
    -- Penalize for numeric streets if one is completely numeric and the other is not
    -- This is to minimize on highways like 3A being matched with numbered streets since streets are usually number followed by 2 characters e.g nth ave and highways are just number with optional letter for name
        IF  (streetNameB ~ E'[a-zA-Z]{2,10}' AND NOT (streetNameA ~ E'[a-zA-Z]{2,10}') ) OR (streetNameA ~ E'[a-zA-Z]{2,10}' AND NOT (streetNameB ~ E'[a-zA-Z]{2,10}') ) THEN
            result := result + levenshtein_ignore_case(streetNameA, streetNameB) * nameWeight;
        END IF;
    END IF;
  ELSE
    IF var_verbose THEN
      RAISE NOTICE 'rate_attributes() - Street names cannot be null!';
    END IF;
    RETURN NULL;
  END IF;
  result := result + levenshtein_ignore_case(cull_null(streetTypeA), cull_null(streetTypeB)) *
      typeWeight;
  result := result + levenshtein_ignore_case(cull_null(dirsA), cull_null(dirsB)) *
      directionWeight;
  return result;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.rate_attributes(dirpa character varying, dirpb character varying, streetnamea character varying, streetnameb character varying, streettypea character varying, streettypeb character varying, dirsa character varying, dirsb character varying, locationa character varying, locationb character varying, prequalabr character varying)
 RETURNS integer
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
DECLARE
  result INTEGER := 0;
  locationWeight INTEGER := 14;
  var_verbose BOOLEAN := FALSE;
BEGIN
  IF locationA IS NOT NULL AND locationB IS NOT NULL THEN
    result := levenshtein_ignore_case(locationA, locationB);
  ELSE
    IF var_verbose THEN
      RAISE NOTICE 'rate_attributes() - Location names cannot be null!';
    END IF;
    RETURN NULL;
  END IF;
  result := result + rate_attributes($1, $2, streetNameA, streetNameB, $5, $6, $7, $8,prequalabr);
  RETURN result;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.reverse_geocode(pt geometry, include_strnum_range boolean DEFAULT false, OUT intpt geometry[], OUT addy norm_addy[], OUT street character varying[])
 RETURNS record
 LANGUAGE plpgsql
 STABLE COST 1000
AS $function$
DECLARE
  var_redge RECORD;
  var_state text := NULL;
  var_stusps text := NULL;
  var_countyfp text := NULL;
  var_addy NORM_ADDY;
  var_addy_alt NORM_ADDY;
  var_nstrnum numeric(10);
  var_primary_line geometry := NULL;
  var_primary_dist numeric(10,2) ;
  var_pt geometry;
  var_place varchar;
  var_county varchar;
  var_stmt text;
  var_debug boolean =  get_geocode_setting('debug_reverse_geocode')::boolean;
  var_rating_highway integer = COALESCE(get_geocode_setting('reverse_geocode_numbered_roads')::integer,0);/**0 no preference, 1 prefer highway number, 2 prefer local name **/
  var_zip varchar := NULL;
  var_primary_fullname varchar := '';
BEGIN
	IF pt IS NULL THEN
		RETURN;
	ELSE
		IF ST_SRID(pt) = 4269 THEN
			var_pt := pt;
		ELSIF ST_SRID(pt) > 0 THEN
			var_pt := ST_Transform(pt, 4269);
		ELSE --If srid is unknown, assume its 4269
			var_pt := ST_SetSRID(pt, 4269);
		END IF;
		var_pt := ST_SnapToGrid(var_pt, 0.00005); /** Get rid of floating point junk that would prevent intersections **/
	END IF;
	-- Determine state tables to check
	-- this is needed to take advantage of constraint exclusion
	IF var_debug THEN
		RAISE NOTICE 'Get matching states start: %', clock_timestamp();
	END IF;
	SELECT statefp, stusps INTO var_state, var_stusps FROM state WHERE ST_Intersects(the_geom, var_pt) LIMIT 1;
	IF var_debug THEN
		RAISE NOTICE 'Get matching states end: % -  %', var_state, clock_timestamp();
	END IF;
	IF var_state IS NULL THEN
		-- We don't have any data for this state
		RETURN;
	END IF;
	IF var_debug THEN
		RAISE NOTICE 'Get matching counties start: %', clock_timestamp();
	END IF;
	-- locate county
	var_stmt := 'SELECT countyfp, name  FROM  county WHERE  statefp =  $1 AND ST_Intersects(the_geom, $2) LIMIT 1;';
	EXECUTE var_stmt INTO var_countyfp, var_county USING var_state, var_pt ;

	--locate zip
	var_stmt := 'SELECT zcta5ce  FROM zcta5 WHERE statefp = $1 AND ST_Intersects(the_geom, $2)  LIMIT 1;';
	EXECUTE var_stmt INTO var_zip USING var_state, var_pt;
	-- locate city
	IF var_zip > '' THEN
	      var_addy.zip := var_zip ;
	END IF;

	var_stmt := 'SELECT z.name  FROM place As z WHERE  z.statefp =  $1 AND ST_Intersects(the_geom, $2) LIMIT 1;';
	EXECUTE var_stmt INTO var_place USING var_state, var_pt ;
	IF var_place > '' THEN
			var_addy.location := var_place;
	ELSE
		var_stmt := 'SELECT z.name  FROM cousub As z WHERE  z.statefp =  $1 AND ST_Intersects(the_geom, $2) LIMIT 1;';
		EXECUTE var_stmt INTO var_place USING var_state, var_pt ;
		IF var_place > '' THEN
			var_addy.location := var_place;
		-- ELSIF var_zip > '' THEN
		-- 	SELECT z.city INTO var_place FROM zip_lookup_base As z WHERE  z.statefp =  var_state AND z.county = var_county AND z.zip = var_zip LIMIT 1;
		-- 	var_addy.location := var_place;
		END IF;
	END IF;

	IF var_debug THEN
		RAISE NOTICE 'Get matching counties end: % - %',var_countyfp,  clock_timestamp();
	END IF;
	IF var_countyfp IS NULL THEN
		-- We don't have any data for this county
		RETURN;
	END IF;

	var_addy.stateAbbrev = var_stusps;

	-- Find the street edges that this point is closest to with tolerance of 0.005 but only consider the edge if the point is contained in the right or left face
	-- Then order addresses by proximity to road
	IF var_debug THEN
		RAISE NOTICE 'Get matching edges start: %', clock_timestamp();
	END IF;

	var_stmt := '
	    WITH ref AS (
	        SELECT ' || quote_literal(var_pt::text) || '::geometry As ref_geom ) ,
			f AS
			( SELECT faces.* FROM faces  CROSS JOIN ref
			WHERE faces.statefp = ' || quote_literal(var_state) || ' AND faces.countyfp = ' || quote_literal(var_countyfp) || '
				AND ST_Intersects(faces.the_geom, ref_geom)
				    ),
			e AS
			( SELECT edges.tlid , edges.statefp, edges.the_geom, CASE WHEN edges.tfidr = f.tfid THEN ''R'' WHEN edges.tfidl = f.tfid THEN ''L'' ELSE NULL END::varchar As eside,
                    ST_ClosestPoint(edges.the_geom,ref_geom) As center_pt, ref_geom
				FROM edges INNER JOIN f ON (f.statefp = edges.statefp AND (edges.tfidr = f.tfid OR edges.tfidl = f.tfid))
				    CROSS JOIN ref
			WHERE edges.statefp = ' || quote_literal(var_state) || ' AND edges.countyfp = ' || quote_literal(var_countyfp) || '
				AND ST_DWithin(edges.the_geom, ref.ref_geom, 0.01) AND (edges.mtfcc LIKE ''S%'') --only consider streets and roads
				  )	,
			ea AS
			(SELECT e.statefp, e.tlid, a.fromhn, a.tohn, e.center_pt, ref_geom, a.zip, a.side, e.the_geom
				FROM e LEFT JOIN addr As a ON (a.statefp = ' || quote_literal(var_state) || '  AND e.tlid = a.tlid and e.eside = a.side)
				)
		SELECT *
		FROM (SELECT DISTINCT ON(tlid,side)  foo.fullname, foo.predirabrv, foo.streetname, foo.sufdirabrv, foo.streettypeabbrev, foo.zip,  foo.center_pt,
			  side, to_number(CASE WHEN trim(fromhn) ~ ''^[0-9]+$'' THEN fromhn ELSE NULL END,''99999999'')  As fromhn, to_number(CASE WHEN trim(tohn) ~ ''^[0-9]+$'' THEN tohn ELSE NULL END,''99999999'') As tohn,
			  ST_GeometryN(ST_Multi(line),1) As line, dist
		FROM
		  (SELECT e.tlid, e.the_geom As line, n.fullname, COALESCE(n.prequalabr || '' '','''')  || n.name AS streetname, n.predirabrv, COALESCE(suftypabrv, pretypabrv) As streettypeabbrev,
		      n.sufdirabrv, e.zip, e.side, e.fromhn, e.tohn , e.center_pt,
		          ST_DistanceSphere(ST_SetSRID(e.center_pt,4326),ST_SetSRID(ref_geom,4326)) As dist
				FROM ea AS e
					LEFT JOIN (SELECT featnames.* FROM featnames
			    WHERE featnames.statefp = ' || quote_literal(var_state) ||'   ) AS n ON (n.statefp =  e.statefp AND n.tlid = e.tlid)
				ORDER BY dist LIMIT 50 ) As foo
				ORDER BY foo.tlid, foo.side, ';

	    -- for numbered street/road use var_rating_highway to determine whether to prefer numbered or not (0 no pref, 1 prefer numbered, 2 prefer named)
		var_stmt := var_stmt || ' CASE $1 WHEN 0 THEN 0  WHEN 1 THEN CASE WHEN foo.fullname ~ ''[0-9]+'' THEN 0 ELSE 1 END ELSE CASE WHEN foo.fullname > '''' AND NOT (foo.fullname ~ ''[0-9]+'') THEN 0 ELSE 1 END END ';
		var_stmt := var_stmt || ',  foo.fullname ASC NULLS LAST, dist LIMIT 50) As f ORDER BY f.dist, CASE WHEN fullname > '''' THEN 0 ELSE 1 END '; --don't bother penalizing for distance if less than 20 meters

	IF var_debug = true THEN
	    RAISE NOTICE 'Statement 1: %', replace(var_stmt, '$1', var_rating_highway::text);
	END IF;

    FOR var_redge IN EXECUTE var_stmt USING var_rating_highway LOOP
        IF var_debug THEN
            RAISE NOTICE 'Start Get matching edges loop: %,%', var_primary_line, clock_timestamp();
        END IF;
        IF var_primary_line IS NULL THEN --this is the first time in the loop and our primary guess
            var_primary_line := var_redge.line;
            var_primary_dist := var_redge.dist;
        END IF;

        IF var_redge.fullname IS NOT NULL AND COALESCE(var_primary_fullname,'') = '' THEN -- this is the first non-blank name we are hitting grab info
            var_primary_fullname := var_redge.fullname;
            var_addy.streetname = var_redge.streetname;
            var_addy.streettypeabbrev := var_redge.streettypeabbrev;
            var_addy.predirabbrev := var_redge.predirabrv;
			var_addy.postDirAbbrev := var_redge.sufdirabrv;
        END IF;

        IF ST_Intersects(var_redge.line, var_primary_line) THEN
            var_addy.streetname := var_redge.streetname;

            var_addy.streettypeabbrev := var_redge.streettypeabbrev;
            var_addy.address := var_nstrnum;
            IF  var_redge.fromhn IS NOT NULL THEN
                --interpolate the number -- note that if fromhn > tohn we will be subtracting which is what we want
                var_nstrnum := (var_redge.fromhn + ST_LineLocatePoint(var_redge.line, var_pt)*(var_redge.tohn - var_redge.fromhn))::numeric(10);
                -- The odd even street number side of street rule
                IF (var_nstrnum  % 2)  != (var_redge.tohn % 2) THEN
                    var_nstrnum := CASE WHEN var_nstrnum + 1 NOT BETWEEN var_redge.fromhn AND var_redge.tohn THEN var_nstrnum - 1 ELSE var_nstrnum + 1 END;
                END IF;
                var_addy.address := var_nstrnum;
            END IF;
            IF var_redge.zip > ''  THEN
                var_addy.zip := var_redge.zip;
            ELSE
                var_addy.zip := var_zip;
            END IF;
            -- IF var_redge.location > '' THEN
            --     var_addy.location := var_redge.location;
            -- ELSE
            --     var_addy.location := var_place;
            -- END IF;

            -- This is a cross streets - only add if not the primary adress street
            IF var_redge.fullname > '' AND var_redge.fullname <> var_primary_fullname THEN
                street := array_append(street, (CASE WHEN include_strnum_range THEN COALESCE(var_redge.fromhn::varchar, '')::varchar || COALESCE(' - ' || var_redge.tohn::varchar,'')::varchar || ' '::varchar  ELSE '' END::varchar ||  COALESCE(var_redge.fullname::varchar,''))::varchar);
            END IF;

            -- consider this a potential address
            IF (var_redge.dist < var_primary_dist*1.1 OR var_redge.dist < 20)   THEN
                 -- We only consider this a possible address if it is really close to our point
                 intpt := array_append(intpt,var_redge.center_pt);
                -- note that ramps don't have names or addresses but they connect at the edge of a range
                -- so for ramps the address of connecting is still useful
                IF var_debug THEN
                    RAISE NOTICE 'Current addresses: %, last added, %, street: %, %', addy, var_addy, var_addy.streetname, clock_timestamp();
                END IF;
                 addy := array_append(addy, var_addy);

                -- Use current values streetname for previous value if previous value has no streetname
				IF var_addy.streetname > '' AND array_upper(addy,1) > 1 AND COALESCE(addy[array_upper(addy,1) - 1].streetname, '') = ''  THEN
					-- the match is probably an offshoot of some sort
					-- replace prior entry with streetname of new if prior had no streetname
					var_addy_alt := addy[array_upper(addy,1)- 1];
					IF var_debug THEN
						RAISE NOTICE 'Replacing answer : %, %', addy[array_upper(addy,1) - 1], clock_timestamp();
					END IF;
					var_addy_alt.streetname := var_addy.streetname;
					var_addy_alt.streettypeabbrev := var_addy.streettypeabbrev;
                    var_addy_alt.predirabbrev := var_addy.predirabbrev;
					var_addy_alt.postDirAbbrev := var_addy.postDirAbbrev;
					addy[array_upper(addy,1) - 1 ] := var_addy_alt;
					IF var_debug THEN
						RAISE NOTICE 'Replaced with : %, %', var_addy_alt, clock_timestamp();
					END IF;
				END IF;

				IF var_debug THEN
					RAISE NOTICE 'End Get matching edges loop: %', clock_timestamp();
					RAISE NOTICE 'Final addresses: %, %', addy, clock_timestamp();
				END IF;

            END IF;
        END IF;

    END LOOP;

    -- not matching roads or streets, just return basic info
    IF NOT FOUND THEN
        addy := array_append(addy,var_addy);
        IF var_debug THEN
            RAISE NOTICE 'No address found: adding: % street: %, %', var_addy, var_addy.streetname, clock_timestamp();
        END IF;
    END IF;
    IF var_debug THEN
        RAISE NOTICE 'current array count : %, %', array_upper(addy,1), clock_timestamp();
    END IF;

    RETURN;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.set_geocode_setting(setting_name text, setting_value text)
 RETURNS text
 LANGUAGE sql
AS $function$
INSERT INTO geocode_settings(name, setting, unit, category, short_desc)
SELECT name, setting, unit, category, short_desc
    FROM geocode_settings_default
    WHERE name NOT IN(SELECT name FROM geocode_settings);

UPDATE geocode_settings SET setting = $2 WHERE name = $1
	RETURNING setting;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.setsearchpathforinstall(a_schema_name character varying)
 RETURNS text
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
	var_result text;
	var_cur_search_path text;
BEGIN
	SELECT reset_val INTO var_cur_search_path FROM pg_settings WHERE name = 'search_path';

	EXECUTE 'SET search_path = ' || quote_ident(a_schema_name) || ', ' || var_cur_search_path;
	var_result := a_schema_name || ' has been made primary for install ';
  RETURN var_result;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.state_extract(rawinput character varying)
 RETURNS character varying
 LANGUAGE plpgsql
 STABLE
AS $function$
DECLARE
  tempInt INTEGER;
  tempString VARCHAR;
  state VARCHAR;
  stateAbbrev VARCHAR;
  result VARCHAR;
  rec RECORD;
  test BOOLEAN;
  ws VARCHAR;
  var_verbose boolean := false;
BEGIN
  ws := E'[ ,.\t\n\f\r]';

  -- If there is a trailing space or , get rid of it
  -- this is to handle case where people use , instead of space to separate state and zip
  -- such as '2450 N COLORADO ST, PHILADELPHIA, PA, 19132' instead of '2450 N COLORADO ST, PHILADELPHIA, PA 19132'

  --tempString := regexp_replace(rawInput, E'(.*)' || ws || '+', E'\\1');
  tempString := btrim(rawInput, ', ');
  -- Separate out the last word of the state, and use it to compare to
  -- the state lookup table to determine the entire name, as well as the
  -- abbreviation associated with it.  The zip code may or may not have
  -- been found.
  tempString := substring(tempString from ws || E'+([^ ,.\t\n\f\r0-9]*?)$');
  IF var_verbose THEN RAISE NOTICE 'state_extract rawInput: % tempString: %', rawInput, tempString; END IF;
  SELECT INTO tempInt count(*) FROM (select distinct abbrev from state_lookup
      WHERE upper(abbrev) = upper(tempString)) as blah;
  IF tempInt = 1 THEN
    state := tempString;
    SELECT INTO stateAbbrev abbrev FROM (select distinct abbrev from
        state_lookup WHERE upper(abbrev) = upper(tempString)) as blah;
  ELSE
    SELECT INTO tempInt count(*) FROM state_lookup WHERE upper(name)
        like upper('%' || tempString);
    IF tempInt >= 1 THEN
      FOR rec IN SELECT name from state_lookup WHERE upper(name)
          like upper('%' || tempString) LOOP
        SELECT INTO test texticregexeq(rawInput, name) FROM state_lookup
            WHERE rec.name = name;
        IF test THEN
          SELECT INTO stateAbbrev abbrev FROM state_lookup
              WHERE rec.name = name;
          state := substring(rawInput, '(?i)' || rec.name);
          EXIT;
        END IF;
      END LOOP;
    ELSE
      -- No direct match for state, so perform fuzzy match.
      SELECT INTO tempInt count(*) FROM state_lookup
          WHERE soundex(tempString) = end_soundex(name);
      IF tempInt >= 1 THEN
        FOR rec IN SELECT name, abbrev FROM state_lookup
            WHERE soundex(tempString) = end_soundex(name) LOOP
          tempInt := count_words(rec.name);
          tempString := get_last_words(rawInput, tempInt);
          test := TRUE;
          FOR i IN 1..tempInt LOOP
            IF soundex(split_part(tempString, ' ', i)) !=
               soundex(split_part(rec.name, ' ', i)) THEN
              test := FALSE;
            END IF;
          END LOOP;
          IF test THEN
            state := tempString;
            stateAbbrev := rec.abbrev;
            EXIT;
          END IF;
        END LOOP;
      END IF;
    END IF;
  END IF;

  IF state IS NOT NULL AND stateAbbrev IS NOT NULL THEN
    result := state || ':' || stateAbbrev;
  END IF;

  RETURN result;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.topology_load_tiger(toponame character varying, region_type character varying, region_id character varying)
 RETURNS text
 LANGUAGE plpgsql
 COST 1000
AS $function$
DECLARE
 	var_sql text;
 	var_rgeom geometry;
 	var_statefp text;
 	var_rcnt bigint;
 	var_result text := '';
 	var_srid int := 4269;
 	var_precision double precision := 0;
BEGIN
	CASE region_type
		WHEN 'place' THEN
			SELECT the_geom , statefp FROM place INTO var_rgeom, var_statefp WHERE plcidfp = region_id;
		WHEN 'county' THEN
			SELECT the_geom, statefp FROM county INTO var_rgeom, var_statefp WHERE cntyidfp = region_id;
		ELSE
			RAISE EXCEPTION 'Region type % IS NOT SUPPORTED', region_type;
	END CASE;
	SELECT srid, precision FROM topology.topology into var_srid, var_precision
                WHERE name = toponame;
	var_sql := '
	CREATE TEMPORARY TABLE tmp_edge
   				AS
	WITH te AS
   			(SELECT tlid,  ST_GeometryN(ST_SnapToGrid(ST_Transform(ST_LineMerge(the_geom),$3),$4),1) As geom, tnidf, tnidt, tfidl, tfidr , the_geom As orig_geom
									FROM tiger.edges
									WHERE statefp = $1 AND ST_Covers($2, the_geom)
										)
					SELECT DISTINCT ON (t.tlid) t.tlid As edge_id,t.geom
                        , t.tnidf As start_node, t.tnidt As end_node, COALESCE(t.tfidl,0) As left_face
                        , COALESCE(t.tfidr,0) As right_face, COALESCE(tl.tlid, t.tlid) AS next_left_edge,  COALESCE(tr.tlid, t.tlid) As next_right_edge, t.orig_geom
						FROM
							te AS t LEFT JOIN te As tl ON (t.tnidf = tl.tnidt AND t.tfidl = tl.tfidl)
							 LEFT JOIN te As tr ON (t.tnidt = tr.tnidf AND t.tfidr = tr.tfidr)
						';
	EXECUTE var_sql USING var_statefp, var_rgeom, var_srid, var_precision;
	GET DIAGNOSTICS var_rcnt = ROW_COUNT;
	var_result := var_rcnt::text || ' edges holding in temporary. ';
	var_sql := 'ALTER TABLE tmp_edge ADD CONSTRAINT pk_tmp_edge PRIMARY KEY(edge_id );';
	EXECUTE var_sql;
	-- CREATE node indexes on temporary edges
	var_sql := 'CREATE INDEX idx_tmp_edge_start_node ON tmp_edge USING btree (start_node ); CREATE INDEX idx_tmp_edge_end_node ON tmp_edge USING btree (end_node );';

	EXECUTE var_sql;

	-- CREATE face indexes on temporary edges
	var_sql := 'CREATE INDEX idx_tmp_edge_left_face ON tmp_edge USING btree (left_face ); CREATE INDEX idx_tmp_edge_right_face ON tmp_edge USING btree (right_face );';

	EXECUTE var_sql;

	-- CREATE edge indexes on temporary edges
	var_sql := 'CREATE INDEX idx_tmp_edge_next_left_edge ON tmp_edge USING btree (next_left_edge ); CREATE INDEX idx_tmp_edge_next_right_edge ON tmp_edge USING btree (next_right_edge);';

	EXECUTE var_sql;

	-- start load in faces
	var_sql := 'INSERT INTO ' || quote_ident(toponame) || '.face(face_id, mbr)
						SELECT f.tfid, ST_Envelope(ST_Transform(f.the_geom,$3)) As mbr
							FROM tiger.faces AS f
								WHERE statefp = $1 AND
								(  tfid IN(SELECT left_face FROM tmp_edge)
									OR tfid IN(SELECT right_face FROM tmp_edge) OR ST_Covers($2, the_geom) )
							AND tfid NOT IN(SELECT face_id FROM ' || quote_ident(toponame) || '.face) ';
	EXECUTE var_sql USING var_statefp, var_rgeom, var_srid;
	GET DIAGNOSTICS var_rcnt = ROW_COUNT;
	var_result := var_result || var_rcnt::text || ' faces added. ';
   -- end load in faces

   -- add remaining missing edges of present faces --
   var_sql := 'INSERT INTO tmp_edge(edge_id, geom, start_node, end_node, left_face, right_face, next_left_edge, next_right_edge, orig_geom)
   			WITH te AS
   			(SELECT tlid,  ST_GeometryN(ST_SnapToGrid(ST_Transform(ST_LineMerge(the_geom),$2),$3),1) As geom, tnidf, tnidt, tfidl, tfidr, the_geom As orig_geom
									FROM tiger.edges
									WHERE statefp = $1 AND
									 (tfidl IN(SELECT face_id FROM ' || quote_ident(toponame) || '.face)
				OR tfidr IN(SELECT face_id FROM ' || quote_ident(toponame) || '.face) )
				AND tlid NOT IN(SELECT edge_id FROM tmp_edge)
				 )

			SELECT DISTINCT ON (t.tlid) t.tlid As edge_id,t.geom
                        , t.tnidf As start_node, t.tnidt As end_node, t.tfidl As left_face
                        , t.tfidr As right_face, tl.tlid AS next_left_edge,  tr.tlid As next_right_edge, t.orig_geom
				FROM
						te AS t LEFT JOIN te As tl
								ON (t.tnidf = tl.tnidt AND t.tfidl = tl.tfidl)
			LEFT JOIN te As tr ON (t.tnidt = tr.tnidf AND t.tfidr = tr.tfidr)
			';
	EXECUTE var_sql USING var_statefp, var_srid, var_precision;
	GET DIAGNOSTICS var_rcnt = ROW_COUNT;
	var_result := var_result || var_rcnt::text || ' edges of faces added. ';
   	-- start load in nodes
	var_sql := 'INSERT INTO ' || quote_ident(toponame) || '.node(node_id, geom)
					SELECT DISTINCT ON(tnid) tnid, geom
						FROM
						(
							SELECT start_node AS tnid, ST_StartPoint(e.geom) As geom
								FROM tmp_edge As e LEFT JOIN ' || quote_ident(toponame) || '.node AS n ON e.start_node = n.node_id
						UNION ALL
							SELECT end_node AS tnid, ST_EndPoint(e.geom) As geom
							FROM tmp_edge As e LEFT JOIN ' || quote_ident(toponame) || '.node AS n ON e.end_node = n.node_id
							WHERE n.node_id IS NULL) As f
							WHERE tnid NOT IN(SELECT node_id FROM  ' || quote_ident(toponame) || '.node)
					 ';
	EXECUTE var_sql USING var_statefp, var_rgeom;
	GET DIAGNOSTICS var_rcnt = ROW_COUNT;
	var_result := var_result || ' ' || var_rcnt::text || ' nodes added. ';

   -- end load in nodes
   -- start Mark which nodes are contained in faces
   	var_sql := 'UPDATE ' || quote_ident(toponame) || '.node AS n
					SET containing_face = f.tfid
						FROM (SELECT tfid, the_geom
							FROM tiger.faces WHERE statefp = $1
							AND tfid IN(SELECT face_id FROM ' || quote_ident(toponame) || '.face)
							) As f
						WHERE ST_ContainsProperly(f.the_geom, ST_Transform(n.geom,4269)) ';
	EXECUTE var_sql USING var_statefp, var_rgeom;
	GET DIAGNOSTICS var_rcnt = ROW_COUNT;
	var_result := var_result || ' ' || var_rcnt::text || ' nodes contained in a face. ';
   -- end Mark nodes contained in faces

   -- Set orphan left right to itself and set edges with missing faces to world face
   var_sql := 'UPDATE tmp_edge SET next_left_edge = -1*edge_id WHERE next_left_edge IS NULL OR next_left_edge NOT IN(SELECT edge_id FROM tmp_edge);
        UPDATE tmp_edge SET next_right_edge = -1*edge_id WHERE next_right_edge IS NULL OR next_right_edge NOT IN(SELECT edge_id FROM tmp_edge);
        UPDATE tmp_edge SET left_face = 0 WHERE left_face NOT IN(SELECT face_id FROM ' || quote_ident(toponame) || '.face);
        UPDATE tmp_edge SET right_face = 0 WHERE right_face NOT IN(SELECT face_id FROM ' || quote_ident(toponame) || '.face);';
   EXECUTE var_sql;

   -- force edges start and end points to match the start and end nodes --
   var_sql := 'UPDATE tmp_edge SET geom = ST_SetPoint(ST_SetPoint(tmp_edge.geom, 0, s.geom), ST_NPoints(tmp_edge.geom) - 1,e.geom)
                FROM ' || quote_ident(toponame) || '.node AS s, ' || quote_ident(toponame) || '.node As e
                WHERE s.node_id = tmp_edge.start_node AND e.node_id = tmp_edge.end_node AND
                    ( NOT ST_Equals(s.geom, ST_StartPoint(tmp_edge.geom) ) OR NOT ST_Equals(e.geom, ST_EndPoint(tmp_edge.geom) ) ) '  ;
    EXECUTE var_sql;
    GET DIAGNOSTICS var_rcnt = ROW_COUNT;
    var_result := var_result || ' ' || var_rcnt::text || ' edge start end corrected. ';
   -- TODO: Load in edges --
   var_sql := '
   	INSERT INTO ' || quote_ident(toponame) || '.edge(edge_id, geom, start_node, end_node, left_face, right_face, next_left_edge, next_right_edge)
					SELECT t.edge_id, t.geom, t.start_node, t.end_node, COALESCE(t.left_face,0) As left_face, COALESCE(t.right_face,0) As right_face, t.next_left_edge, t.next_right_edge
						FROM
							tmp_edge AS t
							WHERE t.edge_id NOT IN(SELECT edge_id FROM ' || quote_ident(toponame) || '.edge)
						';
	EXECUTE var_sql USING var_statefp, var_rgeom;
	GET DIAGNOSTICS var_rcnt = ROW_COUNT;
	var_result := var_result || ' ' || var_rcnt::text || ' edges added. ';
	var_sql = 'DROP TABLE tmp_edge;';
	EXECUTE var_sql;
	RETURN var_result;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.utmzone(geometry)
 RETURNS integer
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
DECLARE
    geomgeog geometry;
    zone int;
    pref int;
BEGIN
    geomgeog:=ST_Transform($1,4326);
    IF (ST_Y(geomgeog))>0 THEN
        pref:=32600;
    ELSE
        pref:=32700;
    END IF;
    zone:=floor((ST_X(geomgeog)+180)/6)+1;
    RETURN zone+pref;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION tiger.zip_range(zip text, range_start integer, range_end integer)
 RETURNS character varying[]
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
   SELECT ARRAY(
        SELECT lpad((to_number( CASE WHEN trim(substring($1,1,5)) ~ '^[0-9]+$' THEN $1 ELSE '0' END,'99999')::integer + i)::text, 5, '0')::varchar
        FROM generate_series($2, $3) As i );
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology._asgmledge(edge_id integer, start_node integer, end_node integer, line geometry, visitedtable regclass, nsprefix_in text, prec integer, options integer, idprefix text, gmlver integer)
 RETURNS text
 LANGUAGE plpgsql
AS $function$
DECLARE
  visited bool;
  nsprefix text;
  gml text;
BEGIN

  nsprefix := 'gml:';
  IF nsprefix_in IS NOT NULL THEN
    IF nsprefix_in = '' THEN
      nsprefix = nsprefix_in;
    ELSE
      nsprefix = nsprefix_in || ':';
    END IF;
  END IF;

  gml := '<' || nsprefix || 'Edge ' || nsprefix
    || 'id="' || idprefix || 'E' || edge_id || '">';

  -- Start node
  gml = gml || '<' || nsprefix || 'directedNode orientation="-"';
  -- Do visited bookkeeping if visitedTable was given
  visited = NULL;
  IF visitedTable IS NOT NULL THEN
    EXECUTE 'SELECT true FROM '
            || visitedTable::text
            || ' WHERE element_type = 1 AND element_id = '
            || start_node LIMIT 1 INTO visited;
    IF visited IS NOT NULL THEN
      gml = gml || ' xlink:href="#' || idprefix || 'N' || start_node || '" />';
    ELSE
      -- Mark as visited
      EXECUTE 'INSERT INTO ' || visitedTable::text
        || '(element_type, element_id) VALUES (1, '
        || start_node || ')';
    END IF;
  END IF;
  IF visited IS NULL THEN
    gml = gml || '>';
    gml = gml || topology._AsGMLNode(start_node, NULL, nsprefix_in,
                                     prec, options, idprefix, gmlver);
    gml = gml || '</' || nsprefix || 'directedNode>';
  END IF;

  -- End node
  gml = gml || '<' || nsprefix || 'directedNode';
  -- Do visited bookkeeping if visitedTable was given
  visited = NULL;
  IF visitedTable IS NOT NULL THEN
    EXECUTE 'SELECT true FROM '
            || visitedTable::text
            || ' WHERE element_type = 1 AND element_id = '
            || end_node LIMIT 1 INTO visited;
    IF visited IS NOT NULL THEN
      gml = gml || ' xlink:href="#' || idprefix || 'N' || end_node || '" />';
    ELSE
      -- Mark as visited
      EXECUTE 'INSERT INTO ' || visitedTable::text
        || '(element_type, element_id) VALUES (1, '
        || end_node || ')';
    END IF;
  END IF;
  IF visited IS NULL THEN
    gml = gml || '>';
    gml = gml || topology._AsGMLNode(end_node, NULL, nsprefix_in,
                                     prec, options, idprefix, gmlver);
    gml = gml || '</' || nsprefix || 'directedNode>';
  END IF;

  IF line IS NOT NULL THEN
    gml = gml || '<' || nsprefix || 'curveProperty>'
              || ST_AsGML(gmlver, line, prec, options, nsprefix_in)
              || '</' || nsprefix || 'curveProperty>';
  END IF;

  gml = gml || '</' || nsprefix || 'Edge>';

  RETURN gml;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology._asgmlface(toponame text, face_id integer, visitedtable regclass, nsprefix_in text, prec integer, options integer, idprefix text, gmlver integer)
 RETURNS text
 LANGUAGE plpgsql
AS $function$
DECLARE
  visited bool;
  nsprefix text;
  gml text;
  rec RECORD;
  rec2 RECORD;
  bounds geometry;
BEGIN

  nsprefix := 'gml:';
  IF nsprefix_in IS NOT NULL THEN
    IF nsprefix_in = '' THEN
      nsprefix = nsprefix_in;
    ELSE
      nsprefix = nsprefix_in || ':';
    END IF;
  END IF;

  gml := '<' || nsprefix || 'Face ' || nsprefix
    || 'id="' || idprefix || 'F' || face_id || '">';

  -- Construct the face geometry, then for each polygon:
  FOR rec IN SELECT (ST_DumpRings((ST_Dump(ST_ForceRHR(
    topology.ST_GetFaceGeometry(toponame, face_id)))).geom)).geom
  LOOP

      -- Contents of a directed face are the list of edges
      -- that cover the specific ring
      bounds = ST_Boundary(rec.geom);

      FOR rec2 IN EXECUTE
        'SELECT e.*, ST_LineLocatePoint($1'
        || ', ST_LineInterpolatePoint(e.geom, 0.2)) as pos'
        || ', ST_LineLocatePoint($1'
        || ', ST_LineInterpolatePoint(e.geom, 0.8)) as pos2 FROM '
        || quote_ident(toponame)
        || '.edge e WHERE ( e.left_face = $2'
        || ' OR e.right_face = $2'
        || ') AND ST_Covers($1'
        || ', e.geom) ORDER BY pos'
        USING bounds, face_id
      LOOP

        gml = gml || '<' || nsprefix || 'directedEdge';

        -- if this edge goes in same direction to the
        --       ring bounds, make it with negative orientation
        IF rec2.pos2 > rec2.pos THEN -- edge goes in same direction
          gml = gml || ' orientation="-"';
        END IF;

        -- Do visited bookkeeping if visitedTable was given
        IF visitedTable IS NOT NULL THEN

          EXECUTE 'SELECT true FROM '
            || visitedTable::text
            || ' WHERE element_type = 2 AND element_id = '
            || rec2.edge_id LIMIT 1 INTO visited;
          IF visited THEN
            -- Use xlink:href if visited
            gml = gml || ' xlink:href="#' || idprefix || 'E'
                      || rec2.edge_id || '" />';
            CONTINUE;
          ELSE
            -- Mark as visited otherwise
            EXECUTE 'INSERT INTO ' || visitedTable::text
              || '(element_type, element_id) VALUES (2, '
              || rec2.edge_id || ')';
          END IF;

        END IF;

        gml = gml || '>';

        gml = gml || topology._AsGMLEdge(rec2.edge_id, rec2.start_node,
                                        rec2.end_node, rec2.geom,
                                        visitedTable, nsprefix_in,
                                        prec, options, idprefix, gmlver);
        gml = gml || '</' || nsprefix || 'directedEdge>';

      END LOOP;
    END LOOP;

  gml = gml || '</' || nsprefix || 'Face>';

  RETURN gml;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology._asgmlnode(id integer, point geometry, nsprefix_in text, prec integer, options integer, idprefix text, gmlver integer)
 RETURNS text
 LANGUAGE plpgsql
 IMMUTABLE
AS $function$
DECLARE
  nsprefix text;
  gml text;
BEGIN

  nsprefix := 'gml:';
  IF NOT nsprefix_in IS NULL THEN
    IF nsprefix_in = '' THEN
      nsprefix = nsprefix_in;
    ELSE
      nsprefix = nsprefix_in || ':';
    END IF;
  END IF;

  gml := '<' || nsprefix || 'Node ' || nsprefix
    || 'id="' || idprefix || 'N' || id || '"';
  IF point IS NOT NULL THEN
    gml = gml || '>'
              || '<' || nsprefix || 'pointProperty>'
              || ST_AsGML(gmlver, point, prec, options, nsprefix_in)
              || '</' || nsprefix || 'pointProperty>'
              || '</' || nsprefix || 'Node>';
  ELSE
    gml = gml || '/>';
  END IF;
  RETURN gml;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology._st_adjacentedges(atopology character varying, anode integer, anedge integer)
 RETURNS integer[]
 LANGUAGE plpgsql
 STABLE
AS $function$
DECLARE
  ret integer[];
BEGIN
  WITH edgestar AS (
    SELECT *, count(*) over () AS cnt
    FROM topology.GetNodeEdges(atopology, anode)
  )
  SELECT ARRAY[ (
      SELECT p.edge AS prev FROM edgestar p
      WHERE p.sequence = CASE WHEN m.sequence-1 < 1 THEN cnt
                         ELSE m.sequence-1 END
    ), (
      SELECT p.edge AS prev FROM edgestar p WHERE p.sequence = ((m.sequence)%cnt)+1
    ) ]
  FROM edgestar m
  WHERE edge = anedge
  INTO ret;

  RETURN ret;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology._st_mintolerance(ageom geometry)
 RETURNS double precision
 LANGUAGE sql
 IMMUTABLE STRICT
AS $function$
    SELECT 3.6 * power(10,  - ( 15 - log(coalesce(
      nullif(
        greatest(abs(ST_xmin($1)), abs(ST_ymin($1)),
                 abs(ST_xmax($1)), abs(ST_ymax($1))),
        0),
      1)) ));
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology._st_mintolerance(atopology character varying, ageom geometry)
 RETURNS double precision
 LANGUAGE plpgsql
 STABLE STRICT
AS $function$
DECLARE
  ret FLOAT8;
BEGIN
  SELECT COALESCE(
    NULLIF(precision, 0),
    topology._st_mintolerance($2))
  FROM topology.topology
  WHERE name = $1 INTO ret;
  IF NOT FOUND THEN
    RAISE EXCEPTION
      'No topology with name "%" in topology.topology', atopology;
  END IF;
  return ret;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.addedge(atopology character varying, aline geometry)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
DECLARE
	edgeid int;
	rec RECORD;
  ix geometry;
BEGIN
	--
	-- Atopology and apoint are required
	--
	IF atopology IS NULL OR aline IS NULL THEN
		RAISE EXCEPTION 'Invalid null argument';
	END IF;

	--
	-- Aline must be a linestring
	--
	IF substring(geometrytype(aline), 1, 4) != 'LINE'
	THEN
		RAISE EXCEPTION 'Edge geometry must be a linestring';
	END IF;

	--
	-- Check there's no face registered in the topology
	--
	FOR rec IN EXECUTE 'SELECT count(face_id) FROM '
		|| quote_ident(atopology) || '.face '
		|| ' WHERE face_id != 0 LIMIT 1'
	LOOP
		IF rec.count > 0 THEN
			RAISE EXCEPTION 'AddEdge can only be used against topologies with no faces defined';
		END IF;
	END LOOP;

	--
	-- Check if the edge crosses an existing node
	--
	FOR rec IN EXECUTE 'SELECT node_id FROM '
		|| quote_ident(atopology) || '.node '
		|| 'WHERE ST_Crosses($1, geom)'
    USING aline
	LOOP
		RAISE EXCEPTION 'Edge crosses node %', rec.node_id;
	END LOOP;

	--
	-- Check if the edge intersects an existing edge
	-- on anything but endpoints
	--
	-- Following DE-9 Intersection Matrix represent
	-- the only relation we accept.
	--
	--    F F 1
	--    F * *
	--    1 * 2
	--
	-- Example1: linestrings touching at one endpoint
	--    FF1 F00 102
	--    FF1 F** 1*2 <-- our match
	--
	-- Example2: linestrings touching at both endpoints
	--    FF1 F0F 1F2
	--    FF1 F** 1*2 <-- our match
	--
	FOR rec IN EXECUTE 'SELECT edge_id, geom, ST_Relate($1, geom, 2) as im FROM '
		|| quote_ident(atopology) || '.edge WHERE $1 && geom'
    USING aline
	LOOP

	  IF ST_RelateMatch(rec.im, 'FF1F**1*2') THEN
	    CONTINUE; -- no interior intersection
	  END IF;

	  -- Reuse an EQUAL edge (be it closed or not)
	  IF ST_RelateMatch(rec.im, '1FFF*FFF2') THEN
	      RETURN rec.edge_id;
	  END IF;

	  -- WARNING: the constructive operation might throw an exception
	  BEGIN
	    ix = ST_Intersection(rec.geom, aline);
	  EXCEPTION
	  WHEN OTHERS THEN
	    RAISE NOTICE 'Could not compute intersection between input edge (%) and edge % (%)', aline::text, rec.edge_id, rec.geom::text;
	  END;

	  RAISE EXCEPTION 'Edge intersects (not on endpoints) with existing edge % at or near point %', rec.edge_id, ST_AsText(ST_PointOnSurface(ix));

	END LOOP;

	--
	-- Get new edge id from sequence
	--
	FOR rec IN EXECUTE 'SELECT nextval(' ||
		quote_literal(
			quote_ident(atopology) || '.edge_data_edge_id_seq'
		) || ')'
	LOOP
		edgeid = rec.nextval;
	END LOOP;

	--
	-- Insert the new row
	--
	EXECUTE 'INSERT INTO '
		|| quote_ident(atopology)
		|| '.edge(edge_id, start_node, end_node, '
		|| 'next_left_edge, next_right_edge, '
		|| 'left_face, right_face, '
		|| 'geom) '
		|| ' VALUES('

		-- edge_id
		|| edgeid ||','

		-- start_node
		|| 'topology.addNode('
		|| quote_literal(atopology)
		|| ', ST_StartPoint($1)), '

		-- end_node
		|| 'topology.addNode('
		|| quote_literal(atopology)
		|| ', ST_EndPoint($1)), '

		-- next_left_edge
		|| -edgeid ||','

		-- next_right_edge
		|| edgeid ||','

		-- left_face
		|| '0,'

		-- right_face
		|| '0,'

		-- geom
		|| '$1)'
    USING aline;

	RETURN edgeid;

END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.addface(atopology character varying, apoly geometry, force_new boolean DEFAULT false)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
DECLARE
  bounds geometry;
  symdif geometry;
  faceid int;
  rec RECORD;
  rrec RECORD;
  relate text;
  right_edges int[];
  left_edges int[];
  all_edges geometry;
  old_faceid int;
  old_edgeid int;
  sql text;
  right_side bool;
  edgeseg geometry;
  p1 geometry;
  p2 geometry;
  p3 geometry;
  loc float8;
  segnum int;
  numsegs int;
BEGIN
  --
  -- Atopology and apoly are required
  --
  IF atopology IS NULL OR apoly IS NULL THEN
    RAISE EXCEPTION 'Invalid null argument';
  END IF;

  --
  -- Aline must be a polygon
  --
  IF substring(geometrytype(apoly), 1, 4) != 'POLY'
  THEN
    RAISE EXCEPTION 'Face geometry must be a polygon';
  END IF;

  for rrec IN SELECT (d).* FROM (
    SELECT ST_DumpRings(ST_ForceRHR(apoly)) d
  ) foo
  LOOP -- {
    --
    -- Find all bounds edges, forcing right-hand-rule
    -- to know what's left and what's right...
    --
    bounds = ST_Boundary(rrec.geom);

    sql := 'SELECT e.geom, e.edge_id, e.left_face, e.right_face FROM '
      || quote_ident(atopology)
      || '.edge e, (SELECT $1 as geom) r WHERE r.geom && e.geom'
    ;
    -- RAISE DEBUG 'SQL: %', sql;
    FOR rec IN EXECUTE sql USING bounds
    LOOP -- {
      --RAISE DEBUG 'Edge % has bounding box intersection', rec.edge_id;

      -- Find first non-empty segment of the edge
      numsegs = ST_NumPoints(rec.geom);
      segnum = 1;
      WHILE segnum < numsegs LOOP
        p1 = ST_PointN(rec.geom, segnum);
        p2 = ST_PointN(rec.geom, segnum+1);
        IF ST_Distance(p1, p2) > 0 THEN
          EXIT;
        END IF;
        segnum = segnum + 1;
      END LOOP;

      IF segnum = numsegs THEN
        RAISE WARNING 'Edge % is collapsed', rec.edge_id;
        CONTINUE; -- we don't want to spend time on it
      END IF;

      edgeseg = ST_MakeLine(p1, p2);

      -- Skip non-covered edges
      IF NOT ST_Equals(p2, ST_EndPoint(rec.geom)) THEN
        IF NOT ( _ST_Intersects(bounds, p1) AND _ST_Intersects(bounds, p2) )
        THEN
          --RAISE DEBUG 'Edge % has points % and % not intersecting with ring bounds', rec.edge_id, st_astext(p1), st_astext(p2);
          CONTINUE;
        END IF;
      ELSE
        -- must be a 2-points only edge, let's use Covers (more expensive)
        IF NOT _ST_Covers(bounds, edgeseg) THEN
          --RAISE DEBUG 'Edge % is not covered by ring', rec.edge_id;
          CONTINUE;
        END IF;
      END IF;

      p3 = ST_StartPoint(bounds);
      IF ST_DWithin(edgeseg, p3, 0) THEN
        -- Edge segment covers ring endpoint, See bug #874
        loc = ST_LineLocatePoint(edgeseg, p3);
        -- WARNING: this is as robust as length of edgeseg allows...
        IF loc > 0.9 THEN
          -- shift last point down
          p2 = ST_LineInterpolatePoint(edgeseg, loc - 0.1);
        ELSIF loc < 0.1 THEN
          -- shift first point up
          p1 = ST_LineInterpolatePoint(edgeseg, loc + 0.1);
        ELSE
          -- when ring start point is in between, we swap the points
          p3 = p1; p1 = p2; p2 = p3;
        END IF;
      END IF;

      right_side = ST_LineLocatePoint(bounds, p1) <
                   ST_LineLocatePoint(bounds, p2);


      IF right_side THEN
        right_edges := array_append(right_edges, rec.edge_id);
        old_faceid = rec.right_face;
      ELSE
        left_edges := array_append(left_edges, rec.edge_id);
        old_faceid = rec.left_face;
      END IF;

      IF faceid IS NULL OR faceid = 0 THEN
        faceid = old_faceid;
        old_edgeid = rec.edge_id;
      ELSIF faceid != old_faceid THEN
        RAISE EXCEPTION 'Edge % has face % registered on the side of this face, while edge % has face % on the same side', rec.edge_id, old_faceid, old_edgeid, faceid;
      END IF;

      -- Collect all edges for final full coverage check
      all_edges = ST_Collect(all_edges, rec.geom);

    END LOOP; -- }
  END LOOP; -- }

  IF all_edges IS NULL THEN
    RAISE EXCEPTION 'Found no edges on the polygon boundary';
  END IF;


  --
  -- Check that all edges found, taken togheter,
  -- fully match the ring boundary and nothing more
  --
  -- If the test fail either we need to add more edges
  -- from the polygon ring or we need to split
  -- some of the existing ones.
  --
  bounds = ST_Boundary(apoly);
  IF NOT ST_isEmpty(ST_SymDifference(bounds, all_edges)) THEN
    IF NOT ST_isEmpty(ST_Difference(bounds, all_edges)) THEN
      RAISE EXCEPTION 'Polygon boundary is not fully defined by existing edges at or near point %', ST_AsText(ST_PointOnSurface(ST_Difference(bounds, all_edges)));
    ELSE
      RAISE EXCEPTION 'Existing edges cover polygon boundary and more at or near point % (invalid topology?)', ST_AsText(ST_PointOnSurface(ST_Difference(all_edges, bounds)));
    END IF;
  END IF;

  IF faceid IS NOT NULL AND faceid != 0 THEN
    IF NOT force_new THEN
      RETURN faceid;
    ELSE
    END IF;
  END IF;

  --
  -- Get new face id from sequence
  --
  FOR rec IN EXECUTE 'SELECT nextval(' ||
    quote_literal(
      quote_ident(atopology) || '.face_face_id_seq'
    ) || ')'
  LOOP
    faceid = rec.nextval;
  END LOOP;

  --
  -- Insert new face
  --
  EXECUTE 'INSERT INTO '
    || quote_ident(atopology)
    || '.face(face_id, mbr) VALUES('
    -- face_id
    || faceid || ','
    -- minimum bounding rectangle
    || '$1)'
    USING ST_Envelope(apoly);

  --
  -- Update all edges having this face on the left
  --
  IF left_edges IS NOT NULL THEN
    EXECUTE 'UPDATE '
    || quote_ident(atopology)
    || '.edge_data SET left_face = '
    || quote_literal(faceid)
    || ' WHERE edge_id = ANY('
    || quote_literal(left_edges)
    || ') ';
  END IF;

  --
  -- Update all edges having this face on the right
  --
  IF right_edges IS NOT NULL THEN
    EXECUTE 'UPDATE '
    || quote_ident(atopology)
    || '.edge_data SET right_face = '
    || quote_literal(faceid)
    || ' WHERE edge_id = ANY('
    || quote_literal(right_edges)
    || ') ';
  END IF;

  --
  -- Set left_face/right_face of any contained edge
  --
  EXECUTE 'UPDATE '
    || quote_ident(atopology)
    || '.edge_data SET right_face = '
    || quote_literal(faceid)
    || ', left_face = '
    || quote_literal(faceid)
    || ' WHERE ST_Contains($1, geom)'
    USING apoly;

  --
  -- Set containing_face of any contained node
  --
  EXECUTE 'UPDATE '
    || quote_ident(atopology)
    || '.node SET containing_face = '
    || quote_literal(faceid)
    || ' WHERE containing_face IS NOT NULL AND ST_Contains($1, geom)'
    USING apoly;

  RETURN faceid;

END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.addnode(atopology character varying, apoint geometry)
 RETURNS integer
 LANGUAGE sql
AS $function$
  SELECT topology.AddNode($1, $2, false, false);
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.addnode(atopology character varying, apoint geometry, allowedgesplitting boolean, setcontainingface boolean DEFAULT false)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
DECLARE
	nodeid int;
	rec RECORD;
  containing_face int;
BEGIN
	--
	-- Atopology and apoint are required
	--
	IF atopology IS NULL OR apoint IS NULL THEN
		RAISE EXCEPTION 'Invalid null argument';
	END IF;

	--
	-- Apoint must be a point
	--
	IF substring(geometrytype(apoint), 1, 5) != 'POINT'
	THEN
		RAISE EXCEPTION 'Node geometry must be a point';
	END IF;

	--
	-- Check if a coincident node already exists
	--
	-- We use index AND x/y equality
	--
	FOR rec IN EXECUTE 'SELECT node_id FROM '
		|| quote_ident(atopology) || '.node ' ||
		'WHERE geom && $1 AND ST_X(geom) = ST_X($1) AND ST_Y(geom) = ST_Y($1)'
    USING apoint
	LOOP
		RETURN  rec.node_id;
	END LOOP;

	--
	-- Check if any edge crosses this node
	-- (endpoints are fine)
	--
	FOR rec IN EXECUTE 'SELECT edge_id FROM '
		|| quote_ident(atopology) || '.edge '
		|| 'WHERE ST_DWithin($1, geom, 0) AND '
    || 'NOT ST_Equals($1, ST_StartPoint(geom)) AND '
    || 'NOT ST_Equals($1, ST_EndPoint(geom))'
    USING apoint
	LOOP
    IF allowEdgeSplitting THEN
      RETURN topology.ST_ModEdgeSplit(atopology, rec.edge_id, apoint);
    ELSE
		  RAISE EXCEPTION 'An edge crosses the given node.';
    END IF;
	END LOOP;

  IF setContainingFace THEN
    containing_face := topology.GetFaceByPoint(atopology, apoint, 0);
  ELSE
    containing_face := NULL;
  END IF;

	--
	-- Get new node id from sequence
	--
	FOR rec IN EXECUTE 'SELECT nextval(' ||
		quote_literal(
			quote_ident(atopology) || '.node_node_id_seq'
		) || ')'
	LOOP
		nodeid = rec.nextval;
	END LOOP;

	--
	-- Insert the new row
	--
	EXECUTE 'INSERT INTO ' || quote_ident(atopology)
		|| '.node(node_id, containing_face, geom)
		VALUES(' || nodeid || ',' || coalesce(containing_face::text, 'NULL')
    || ',$1)' USING apoint;

	RETURN nodeid;

END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.addtopogeometrycolumn(character varying, character varying, character varying, character varying, character varying)
 RETURNS integer
 LANGUAGE sql
AS $function$
  SELECT topology.AddTopoGeometryColumn($1, $2, $3, $4, $5, NULL);
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.addtopogeometrycolumn(toponame character varying, schema character varying, tbl character varying, col character varying, ltype character varying, child integer)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
DECLARE
  intltype integer;
  newlevel integer;
  topoid integer;
  rec RECORD;
  newlayer_id integer;
  query text;
BEGIN

  -- Get topology id
  SELECT id INTO topoid
    FROM topology.topology WHERE name = toponame;

  IF NOT FOUND THEN
    RAISE EXCEPTION 'Topology % does not exist', quote_literal(toponame);
  END IF;

  IF ltype ILIKE '%POINT%' OR ltype ILIKE 'PUNTAL' THEN
    intltype = 1;
  ELSIF ltype ILIKE '%LINE%' OR ltype ILIKE 'LINEAL' THEN
    intltype = 2;
  ELSIF ltype ILIKE '%POLYGON%' OR ltype ILIKE 'AREAL' THEN
    intltype = 3;
  ELSIF ltype ILIKE '%COLLECTION%' OR ltype ILIKE 'GEOMETRY' THEN
    intltype = 4;
  ELSE
    RAISE EXCEPTION 'Layer type must be one of POINT,LINE,POLYGON,COLLECTION';
  END IF;

  --
  -- Add new TopoGeometry column in schema.table
  --
  EXECUTE 'ALTER TABLE ' || quote_ident(schema)
    || '.' || quote_ident(tbl)
    || ' ADD COLUMN ' || quote_ident(col)
    || ' topology.TopoGeometry;';

  --
  -- See if child id exists and extract its level
  --
  IF child IS NOT NULL THEN
    SELECT level + 1 FROM topology.layer
      WHERE layer_id = child
        AND topology_id = topoid
      INTO newlevel;
    IF newlevel IS NULL THEN
      RAISE EXCEPTION 'Child layer % does not exist in topology "%"', child, toponame;
    END IF;
  END IF;

  --
  -- Get new layer id from sequence
  --
  EXECUTE 'SELECT nextval(' ||
    quote_literal(
      quote_ident(toponame) || '.layer_id_seq'
    ) || ')' INTO STRICT newlayer_id;

  EXECUTE 'INSERT INTO '
       'topology.layer(topology_id, '
       'layer_id, level, child_id, schema_name, '
       'table_name, feature_column, feature_type) '
       'VALUES ('
    || topoid || ','
    || newlayer_id || ',' || COALESCE(newlevel, 0) || ','
    || COALESCE(child::text, 'NULL') || ','
    || quote_literal(schema) || ','
    || quote_literal(tbl) || ','
    || quote_literal(col) || ','
    || intltype || ');';

  --
  -- Create a sequence for TopoGeometries in this new layer
  --
  EXECUTE 'CREATE SEQUENCE ' || quote_ident(toponame)
    || '.topogeo_s_' || newlayer_id;

  --
  -- Add constraints on TopoGeom column
  --
  EXECUTE 'ALTER TABLE ' || quote_ident(schema)
    || '.' || quote_ident(tbl)
    || ' ADD CONSTRAINT "check_topogeom_' || col || '" CHECK ('
       'topology_id(' || quote_ident(col) || ') = ' || topoid
    || ' AND '
       'layer_id(' || quote_ident(col) || ') = ' || newlayer_id
    || ' AND '
       'type(' || quote_ident(col) || ') = ' || intltype
    || ');';

  --
  -- Add dependency of the feature column on the topology schema
  --
  query = 'INSERT INTO pg_catalog.pg_depend SELECT '
       'fcat.oid, fobj.oid, fsub.attnum, tcat.oid, '
       'tobj.oid, 0, ''n'' '
       'FROM pg_class fcat, pg_namespace fnsp, '
       ' pg_class fobj, pg_attribute fsub, '
       ' pg_class tcat, pg_namespace tobj '
       ' WHERE fcat.relname = ''pg_class'' '
       ' AND fnsp.nspname = ' || quote_literal(schema)
    || ' AND fobj.relnamespace = fnsp.oid '
       ' AND fobj.relname = ' || quote_literal(tbl)
    || ' AND fsub.attrelid = fobj.oid '
       ' AND fsub.attname = ' || quote_literal(col)
    || ' AND tcat.relname = ''pg_namespace'' '
       ' AND tobj.nspname = ' || quote_literal(toponame);

--
-- The only reason to add this dependency is to avoid
-- simple drop of a feature column. Still, drop cascade
-- will remove both the feature column and the sequence
-- corrupting the topology anyway ...
--

  RETURN newlayer_id;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.addtosearchpath(a_schema_name character varying)
 RETURNS text
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
	var_result text;
	var_cur_search_path text;
BEGIN
	SELECT reset_val INTO var_cur_search_path FROM pg_settings WHERE name = 'search_path';
	IF var_cur_search_path LIKE '%' || quote_ident(a_schema_name) || '%' THEN
		var_result := a_schema_name || ' already in database search_path';
	ELSE
		var_cur_search_path := var_cur_search_path || ', '
                        || quote_ident(a_schema_name);
		EXECUTE 'ALTER DATABASE ' || quote_ident(current_database())
                              || ' SET search_path = ' || var_cur_search_path;
		var_result := a_schema_name || ' has been added to end of database search_path ';
	END IF;

	EXECUTE 'SET search_path = ' || var_cur_search_path;

  RETURN var_result;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.asgml(tg topology.topogeometry, visitedtable regclass, nsprefix text)
 RETURNS text
 LANGUAGE sql
AS $function$
 SELECT topology.AsGML($1, $3, 15, 1, $2);
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.asgml(tg topology.topogeometry, nsprefix text, prec integer, options integer, visitedtable regclass, idprefix text)
 RETURNS text
 LANGUAGE sql
AS $function$
 SELECT topology.AsGML($1, $2, $3, $4, $5, $6, 3);
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.asgml(tg topology.topogeometry, visitedtable regclass)
 RETURNS text
 LANGUAGE sql
AS $function$
 SELECT topology.AsGML($1, 'gml', 15, 1, $2);
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.asgml(tg topology.topogeometry)
 RETURNS text
 LANGUAGE sql
 STABLE
AS $function$
 SELECT topology.AsGML($1, 'gml');
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.asgml(tg topology.topogeometry, nsprefix text, prec integer, opts integer)
 RETURNS text
 LANGUAGE sql
 STABLE
AS $function$
 SELECT topology.AsGML($1, $2, $3, $4, NULL);
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.asgml(tg topology.topogeometry, nsprefix_in text, precision_in integer, options_in integer, visitedtable regclass, idprefix text, gmlver integer)
 RETURNS text
 LANGUAGE plpgsql
AS $function$
DECLARE
  nsprefix text;
  precision int;
  options int;
  visited bool;
  toponame text;
  gml text;
  sql text;
  rec RECORD;
  rec2 RECORD;
BEGIN

  nsprefix := 'gml:';
  IF nsprefix_in IS NOT NULL THEN
    IF nsprefix_in = '' THEN
      nsprefix = nsprefix_in;
    ELSE
      nsprefix = nsprefix_in || ':';
    END IF;
  END IF;

  precision := 15;
  IF precision_in IS NOT NULL THEN
    precision = precision_in;
  END IF;

  options := 1;
  IF options_in IS NOT NULL THEN
    options = options_in;
  END IF;

  -- Get topology name (for subsequent queries)
  SELECT name FROM topology.topology into toponame
              WHERE id = tg.topology_id;

  -- Puntual TopoGeometry
  IF tg.type = 1 THEN
    gml = '<' || nsprefix || 'TopoPoint>';
    -- For each defining node, print a directedNode
    FOR rec IN  EXECUTE 'SELECT r.element_id, n.geom from '
      || quote_ident(toponame) || '.relation r LEFT JOIN '
      || quote_ident(toponame) || '.node n ON (r.element_id = n.node_id)'
      || ' WHERE r.layer_id = ' || tg.layer_id
      || ' AND r.topogeo_id = ' || tg.id
    LOOP
      gml = gml || '<' || nsprefix || 'directedNode';
      -- Do visited bookkeeping if visitedTable was given
      IF visitedTable IS NOT NULL THEN
        EXECUTE 'SELECT true FROM '
                || visitedTable::text
                || ' WHERE element_type = 1 AND element_id = '
                || rec.element_id LIMIT 1 INTO visited;
        IF visited IS NOT NULL THEN
          gml = gml || ' xlink:href="#' || idprefix || 'N' || rec.element_id || '" />';
          CONTINUE;
        ELSE
          -- Mark as visited
          EXECUTE 'INSERT INTO ' || visitedTable::text
            || '(element_type, element_id) VALUES (1, '
            || rec.element_id || ')';
        END IF;
      END IF;
      gml = gml || '>';
      gml = gml || topology._AsGMLNode(rec.element_id, rec.geom, nsprefix_in, precision, options, idprefix, gmlver);
      gml = gml || '</' || nsprefix || 'directedNode>';
    END LOOP;
    gml = gml || '</' || nsprefix || 'TopoPoint>';
    RETURN gml;

  ELSIF tg.type = 2 THEN -- lineal
    gml = '<' || nsprefix || 'TopoCurve>';

    FOR rec IN SELECT (ST_Dump(topology.Geometry(tg))).geom
    LOOP
      FOR rec2 IN EXECUTE
        'SELECT e.*, ST_LineLocatePoint($1'
        || ', ST_LineInterpolatePoint(e.geom, 0.2)) as pos'
        || ', ST_LineLocatePoint($1'
        || ', ST_LineInterpolatePoint(e.geom, 0.8)) as pos2 FROM '
        || quote_ident(toponame)
        || '.edge e WHERE ST_Covers($1'
        || ', e.geom) ORDER BY pos'
        -- TODO: add relation to the conditional, to reduce load ?
        USING rec.geom
      LOOP

        gml = gml || '<' || nsprefix || 'directedEdge';

        -- if this edge goes in opposite direction to the
        --       line, make it with negative orientation
        IF rec2.pos2 < rec2.pos THEN -- edge goes in opposite direction
          gml = gml || ' orientation="-"';
        END IF;

        -- Do visited bookkeeping if visitedTable was given
        IF visitedTable IS NOT NULL THEN

          EXECUTE 'SELECT true FROM '
            || visitedTable::text
            || ' WHERE element_type = 2 AND element_id = '
            || rec2.edge_id LIMIT 1 INTO visited;
          IF visited THEN
            -- Use xlink:href if visited
            gml = gml || ' xlink:href="#' || idprefix || 'E' || rec2.edge_id || '" />';
            CONTINUE;
          ELSE
            -- Mark as visited otherwise
            EXECUTE 'INSERT INTO ' || visitedTable::text
              || '(element_type, element_id) VALUES (2, '
              || rec2.edge_id || ')';
          END IF;

        END IF;

        gml = gml || '>';

        gml = gml || topology._AsGMLEdge(rec2.edge_id,
                                        rec2.start_node,
                                        rec2.end_node, rec2.geom,
                                        visitedTable,
                                        nsprefix_in, precision,
                                        options, idprefix, gmlver);

        gml = gml || '</' || nsprefix || 'directedEdge>';
      END LOOP;
    END LOOP;

    gml = gml || '</' || nsprefix || 'TopoCurve>';
    return gml;

  ELSIF tg.type = 3 THEN -- areal
    gml = '<' || nsprefix || 'TopoSurface>';

    -- For each defining face, print a directedFace
    FOR rec IN  EXECUTE 'SELECT f.face_id from '
      || quote_ident(toponame) || '.relation r LEFT JOIN '
      || quote_ident(toponame) || '.face f ON (r.element_id = f.face_id)'
      || ' WHERE r.layer_id = ' || tg.layer_id
      || ' AND r.topogeo_id = ' || tg.id
    LOOP
      gml = gml || '<' || nsprefix || 'directedFace';
      -- Do visited bookkeeping if visitedTable was given
      IF visitedTable IS NOT NULL THEN
        EXECUTE 'SELECT true FROM '
                || visitedTable::text
                || ' WHERE element_type = 3 AND element_id = '
                || rec.face_id LIMIT 1 INTO visited;
        IF visited IS NOT NULL THEN
          gml = gml || ' xlink:href="#' || idprefix || 'F' || rec.face_id || '" />';
          CONTINUE;
        ELSE
          -- Mark as visited
          EXECUTE 'INSERT INTO ' || visitedTable::text
            || '(element_type, element_id) VALUES (3, '
            || rec.face_id || ')';
        END IF;
      END IF;
      gml = gml || '>';
      gml = gml || topology._AsGMLFace(toponame, rec.face_id, visitedTable,
                                       nsprefix_in, precision,
                                       options, idprefix, gmlver);
      gml = gml || '</' || nsprefix || 'directedFace>';
    END LOOP;
    gml = gml || '</' || nsprefix || 'TopoSurface>';
    RETURN gml;

  ELSIF tg.type = 4 THEN -- collection
    RAISE EXCEPTION 'Collection TopoGeometries are not supported by AsGML';

  END IF;

  RETURN gml;

END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.asgml(tg topology.topogeometry, nsprefix text, prec integer, options integer, vis regclass)
 RETURNS text
 LANGUAGE sql
AS $function$
 SELECT topology.AsGML($1, $2, $3, $4, $5, '');
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.asgml(tg topology.topogeometry, nsprefix text)
 RETURNS text
 LANGUAGE sql
 STABLE
AS $function$
 SELECT topology.AsGML($1, $2, 15, 1, NULL);
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.astopojson(tg topology.topogeometry, edgemaptable regclass)
 RETURNS text
 LANGUAGE plpgsql
AS $function$
DECLARE
  toponame text;
  json text;
  sql text;
  bounds GEOMETRY;
  rec RECORD;
  rec2 RECORD;
  side int;
  arcid int;
  arcs int[];
  ringtxt TEXT[];
  comptxt TEXT[];
  edges_found BOOLEAN;
  old_search_path TEXT;
  all_faces int[];
  faces int[];
  bounding_edges int[];
  visited_face int;
  shell_faces int[];
  visited_edges int[];
  looking_for_holes BOOLEAN;
BEGIN

  IF tg IS NULL THEN
    RETURN NULL;
  END IF;

  -- Get topology name (for subsequent queries)
  SELECT name FROM topology.topology into toponame
              WHERE id = tg.topology_id;

  -- TODO: implement scale ?

  -- Puntal TopoGeometry, simply delegate to AsGeoJSON
  IF tg.type = 1 THEN
    json := ST_AsGeoJSON(topology.Geometry(tg));
    return json;
  ELSIF tg.type = 2 THEN -- lineal

    FOR rec IN SELECT (ST_Dump(topology.Geometry(tg))).geom
    LOOP -- {

      sql := 'SELECT e.*, ST_LineLocatePoint($1'
            || ', ST_LineInterpolatePoint(e.geom, 0.2)) as pos'
            || ', ST_LineLocatePoint($1'
            || ', ST_LineInterpolatePoint(e.geom, 0.8)) as pos2 FROM '
            || quote_ident(toponame)
            || '.edge e WHERE ST_Covers($1'
            || ', e.geom) ORDER BY pos';
            -- TODO: add relation to the conditional, to reduce load ?
      FOR rec2 IN EXECUTE sql USING rec.geom
      LOOP -- {

        IF edgeMapTable IS NOT NULL THEN
          sql := 'SELECT arc_id-1 FROM ' || edgeMapTable::text || ' WHERE edge_id = $1';
          EXECUTE sql INTO arcid USING rec2.edge_id;
          IF arcid IS NULL THEN
            EXECUTE 'INSERT INTO ' || edgeMapTable::text
              || '(edge_id) VALUES ($1) RETURNING arc_id-1'
            INTO arcid USING rec2.edge_id;
          END IF;
        ELSE
          arcid := rec2.edge_id;
        END IF;

        -- edge goes in opposite direction
        IF rec2.pos2 < rec2.pos THEN
          arcid := -(arcid+1);
        END IF;

        arcs := arcs || arcid;

      END LOOP; -- }

      comptxt := comptxt || ( '[' || array_to_string(arcs, ',') || ']' );
      arcs := NULL;

    END LOOP; -- }

    json := '{ "type": "MultiLineString", "arcs": [' || array_to_string(comptxt,',') || ']}';

    return json;

  ELSIF tg.type = 3 THEN -- areal

    json := '{ "type": "MultiPolygon", "arcs": [';

    EXECUTE 'SHOW search_path' INTO old_search_path;
    EXECUTE 'SET search_path TO ' || quote_ident(toponame) || ',' || old_search_path;

    SELECT array_agg(id) as f
    FROM ( SELECT (topology.GetTopoGeomElements(tg))[1] as id ) as f
    INTO all_faces;


    visited_edges := ARRAY[]::int[];
    faces := all_faces;
    looking_for_holes := false;
    shell_faces := ARRAY[]::int[];

    SELECT array_agg(edge_id)
    FROM edge_data e
    WHERE
         ( e.left_face = ANY ( faces ) OR
           e.right_face = ANY ( faces ) )
    INTO bounding_edges;

    LOOP -- {

      arcs := NULL;
      edges_found := false;


      FOR rec in -- {
WITH RECURSIVE
_edges AS (
  SELECT e.*,
         e.left_face = ANY ( faces ) as lf,
         e.right_face = ANY ( faces ) as rf
  FROM edge e
  WHERE edge_id = ANY (bounding_edges)
          AND NOT e.edge_id = ANY ( visited_edges )
),
_leftmost_non_dangling_edge AS (
  SELECT e.* FROM _edges e WHERE e.lf != e.rf
  ORDER BY ST_XMin(geom), ST_YMin(geom) LIMIT 1
),
_edgepath AS (
  SELECT
    CASE
      WHEN e.lf THEN lme.edge_id
      ELSE -lme.edge_id
    END as signed_edge_id,
    false as back,

    e.lf = e.rf as dangling,
    e.left_face, e.right_face,
    e.lf, e.rf,
    e.next_right_edge, e.next_left_edge

  FROM _edges e, _leftmost_non_dangling_edge lme
  WHERE e.edge_id = abs(lme.edge_id)
    UNION
  SELECT
    CASE
      WHEN p.dangling AND NOT p.back THEN -p.signed_edge_id
      WHEN p.signed_edge_id < 0 THEN p.next_right_edge
      ELSE p.next_left_edge
    END, -- signed_edge_id
    CASE
      WHEN p.dangling AND NOT p.back THEN true
      ELSE false
    END, -- back

    e.lf = e.rf, -- dangling
    e.left_face, e.right_face,
    e.lf, e.rf,
    e.next_right_edge, e.next_left_edge

  FROM _edges e, _edgepath p
  WHERE
    e.edge_id = CASE
      WHEN p.dangling AND NOT p.back THEN abs(p.signed_edge_id)
      WHEN p.signed_edge_id < 0 THEN abs(p.next_right_edge)
      ELSE abs(p.next_left_edge)
    END
)
SELECT abs(signed_edge_id) as edge_id, signed_edge_id, dangling,
        lf, rf, left_face, right_face
FROM _edgepath
      LOOP  -- }{


        IF rec.left_face = ANY (all_faces) AND NOT rec.left_face = ANY (shell_faces) THEN
          shell_faces := shell_faces || rec.left_face;
        END IF;

        IF rec.right_face = ANY (all_faces) AND NOT rec.right_face = ANY (shell_faces) THEN
          shell_faces := shell_faces || rec.right_face;
        END IF;

        visited_edges := visited_edges || rec.edge_id;

        edges_found := true;

        -- TODO: drop ?
        IF rec.dangling THEN
          CONTINUE;
        END IF;

        IF rec.left_face = ANY (all_faces) AND rec.right_face = ANY (all_faces) THEN
          CONTINUE;
        END IF;

        IF edgeMapTable IS NOT NULL THEN
          sql := 'SELECT arc_id-1 FROM ' || edgeMapTable::text || ' WHERE edge_id = $1';
          EXECUTE sql INTO arcid USING rec.edge_id;
          IF arcid IS NULL THEN
            EXECUTE 'INSERT INTO ' || edgeMapTable::text
              || '(edge_id) VALUES ($1) RETURNING arc_id-1'
            INTO arcid USING rec.edge_id;
          END IF;
        ELSE
          arcid := rec.edge_id-1;
        END IF;

        -- Swap sign, use two's complement for negative edges
        IF rec.signed_edge_id >= 0 THEN
          arcid := - ( arcid + 1 );
        END IF;


        arcs := arcid || arcs;

      END LOOP; -- }


      IF NOT edges_found THEN
        IF looking_for_holes THEN
          looking_for_holes := false;
          comptxt := comptxt || ( '[' || array_to_string(ringtxt, ',') || ']' );
          ringtxt := NULL;
          faces := all_faces;
          shell_faces := ARRAY[]::int[];
        ELSE
          EXIT; -- end of loop
        END IF;
      ELSE
        faces := shell_faces;
        IF arcs IS NOT NULL THEN
          ringtxt := ringtxt || ( '[' || array_to_string(arcs,',') || ']' );
        END IF;
        looking_for_holes := true;
      END IF;

    END LOOP; -- }

    json := json || array_to_string(comptxt, ',') || ']}';

    EXECUTE 'SET search_path TO ' || old_search_path;

  ELSIF tg.type = 4 THEN -- collection
    RAISE EXCEPTION 'Collection TopoGeometries are not supported by AsTopoJSON';

  END IF;

  RETURN json;

END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.cleartopogeom(tg topology.topogeometry)
 RETURNS topology.topogeometry
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
  topology_info RECORD;
  sql TEXT;
BEGIN

  -- Get topology information
  SELECT id, name FROM topology.topology
    INTO topology_info
    WHERE id = topology_id(tg);
  IF NOT FOUND THEN
      RAISE EXCEPTION 'No topology with id "%" in topology.topology', topology_id(tg);
  END IF;

  -- Clear the TopoGeometry contents
  sql := 'DELETE FROM ' || quote_ident(topology_info.name)
        || '.relation WHERE layer_id = '
        || layer_id(tg)
        || ' AND topogeo_id = '
        || id(tg);
  EXECUTE sql;

  RETURN tg;

END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.copytopology(atopology character varying, newtopo character varying)
 RETURNS integer
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
  rec RECORD;
  rec2 RECORD;
  oldtopo_id integer;
  newtopo_id integer;
  n int4;
  ret text;
BEGIN

  SELECT * FROM topology.topology where name = atopology
  INTO strict rec;
  oldtopo_id = rec.id;
  -- TODO: more gracefully handle unexistent topology

  SELECT topology.CreateTopology(newtopo, rec.SRID, rec.precision, rec.hasZ)
  INTO strict newtopo_id;

  -- Copy faces
  EXECUTE 'INSERT INTO ' || quote_ident(newtopo)
    || '.face SELECT * FROM ' || quote_ident(atopology)
    || '.face WHERE face_id != 0';
  -- Update faces sequence
  EXECUTE 'SELECT setval(' || quote_literal(
      quote_ident(newtopo) || '.face_face_id_seq'
    ) || ', (SELECT last_value FROM '
    || quote_ident(atopology) || '.face_face_id_seq))';

  -- Copy nodes
  EXECUTE 'INSERT INTO ' || quote_ident(newtopo)
    || '.node SELECT * FROM ' || quote_ident(atopology)
    || '.node';
  -- Update node sequence
  EXECUTE 'SELECT setval(' || quote_literal(
      quote_ident(newtopo) || '.node_node_id_seq'
    ) || ', (SELECT last_value FROM '
    || quote_ident(atopology) || '.node_node_id_seq))';

  -- Copy edges
  EXECUTE 'INSERT INTO ' || quote_ident(newtopo)
    || '.edge_data SELECT * FROM ' || quote_ident(atopology)
    || '.edge_data';
  -- Update edge sequence
  EXECUTE 'SELECT setval(' || quote_literal(
      quote_ident(newtopo) || '.edge_data_edge_id_seq'
    ) || ', (SELECT last_value FROM '
    || quote_ident(atopology) || '.edge_data_edge_id_seq))';

  -- Copy layers and their TopoGeometry sequences
  FOR rec IN SELECT * FROM topology.layer WHERE topology_id = oldtopo_id
  LOOP
    INSERT INTO topology.layer (topology_id, layer_id, feature_type,
      level, child_id, schema_name, table_name, feature_column)
      VALUES (newtopo_id, rec.layer_id, rec.feature_type,
              rec.level, rec.child_id, newtopo,
              'LAYER' ||  rec.layer_id, '');
    -- Create layer's TopoGeometry sequences
    EXECUTE 'SELECT last_value FROM '
      || quote_ident(atopology) || '.topogeo_s_' || rec.layer_id
      INTO STRICT n;
    EXECUTE 'CREATE SEQUENCE ' || quote_ident(newtopo)
      || '.topogeo_s_' || rec.layer_id;
    EXECUTE 'SELECT setval(' || quote_literal(
      quote_ident(newtopo) || '.topogeo_s_' || rec.layer_id
      ) || ', ' || n || ')';
  END LOOP;

  -- Copy TopoGeometry definitions
  EXECUTE 'INSERT INTO ' || quote_ident(newtopo)
    || '.relation SELECT * FROM ' || quote_ident(atopology)
    || '.relation';

  RETURN newtopo_id;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.createtopogeom(toponame character varying, tg_type integer, layer_id integer, tg_objs topology.topoelementarray)
 RETURNS topology.topogeometry
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
  i integer;
  dims varchar;
  outerdims varchar;
  innerdims varchar;
  obj_type integer;
  obj_id integer;
  ret topology.TopoGeometry;
  rec RECORD;
  layertype integer;
  layerlevel integer;
  layerchild integer;
BEGIN

  IF tg_type < 1 OR tg_type > 4 THEN
    RAISE EXCEPTION 'Invalid TopoGeometry type % (must be in the range 1..4)', tg_type;
  END IF;

  -- Get topology id into return TopoGeometry
  SELECT id INTO ret.topology_id
    FROM topology.topology WHERE name = toponame;

  IF NOT FOUND THEN
    RAISE EXCEPTION 'Topology % does not exist', quote_literal(toponame);
  END IF;

  --
  -- Get layer info
  --
  layertype := NULL;
  FOR rec IN EXECUTE 'SELECT * FROM topology.layer'
       ' WHERE topology_id = ' || ret.topology_id
    || ' AND layer_id = ' || layer_id
  LOOP
    layertype = rec.feature_type;
    layerlevel = rec.level;
    layerchild = rec.child_id;
  END LOOP;

  -- Check for existence of given layer id
  IF layertype IS NULL THEN
    RAISE EXCEPTION 'No layer with id % is registered with topology %', layer_id, toponame;
  END IF;

  -- Verify compatibility between layer geometry type and
  -- TopoGeom requested geometry type
  IF layertype != 4 and layertype != tg_type THEN
    RAISE EXCEPTION 'A Layer of type % cannot contain a TopoGeometry of type %', layertype, tg_type;
  END IF;

  -- Set layer id and type in return object
  ret.layer_id = layer_id;
  ret.type = tg_type;

  --
  -- Get new TopoGeo id from sequence
  --
  FOR rec IN EXECUTE 'SELECT nextval(' ||
    quote_literal(
      quote_ident(toponame) || '.topogeo_s_' || layer_id
    ) || ')'
  LOOP
    ret.id = rec.nextval;
  END LOOP;

  -- Loop over outer dimension
  i = array_lower(tg_objs, 1);
  LOOP
    obj_id = tg_objs[i][1];
    obj_type = tg_objs[i][2];

    -- Elements of type 0 represent emptiness, just skip them
    IF obj_type = 0 THEN
      IF obj_id != 0 THEN
        RAISE EXCEPTION 'Malformed empty topo element {0,%} -- id must be 0 as well', obj_id;
      END IF;
    ELSE
      IF layerlevel = 0 THEN -- array specifies lower-level objects
        IF tg_type != 4 and tg_type != obj_type THEN
          RAISE EXCEPTION 'A TopoGeometry of type % cannot contain topology elements of type %', tg_type, obj_type;
        END IF;
      ELSE -- array specifies lower-level topogeometries
        IF obj_type != layerchild THEN
          RAISE EXCEPTION 'TopoGeom element layer do not match TopoGeom child layer';
        END IF;
        -- TODO: verify that the referred TopoGeometry really
        -- exists in the relation table ?
      END IF;

      --RAISE NOTICE 'obj:% type:% id:%', i, obj_type, obj_id;

      --
      -- Insert record into the Relation table
      --
      EXECUTE 'INSERT INTO '||quote_ident(toponame)
        || '.relation(topogeo_id, layer_id, '
           'element_id,element_type) '
           ' VALUES ('||ret.id
        ||','||ret.layer_id
        || ',' || obj_id || ',' || obj_type || ');';
    END IF;

    i = i+1;
    IF i > array_upper(tg_objs, 1) THEN
      EXIT;
    END IF;
  END LOOP;

  RETURN ret;

END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.createtopogeom(toponame character varying, tg_type integer, layer_id integer)
 RETURNS topology.topogeometry
 LANGUAGE sql
 STRICT
AS $function$
  SELECT topology.CreateTopoGeom($1,$2,$3,'{{0,0}}');
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.createtopology(character varying)
 RETURNS integer
 LANGUAGE sql
 STRICT
AS $function$ SELECT topology.CreateTopology($1, ST_SRID('POINT EMPTY'::geometry), 0); $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.createtopology(atopology character varying, srid integer, prec double precision, hasz boolean)
 RETURNS integer
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
  rec RECORD;
  topology_id integer;
  ndims integer;
BEGIN

--  FOR rec IN SELECT * FROM pg_namespace WHERE text(nspname) = atopology
--  LOOP
--    RAISE EXCEPTION 'SQL/MM Spatial exception - schema already exists';
--  END LOOP;

  ndims = 2;
  IF hasZ THEN ndims = 3; END IF;

  ------ Fetch next id for the new topology
  FOR rec IN SELECT nextval('topology.topology_id_seq')
  LOOP
    topology_id = rec.nextval;
  END LOOP;

  EXECUTE 'CREATE SCHEMA ' || quote_ident(atopology);

  -------------{ face CREATION
  EXECUTE
  'CREATE TABLE ' || quote_ident(atopology) || '.face ('
     'face_id SERIAL,'
     ' CONSTRAINT face_primary_key PRIMARY KEY(face_id)'
     ');';

  -- Add mbr column to the face table
  EXECUTE
  'SELECT AddGeometryColumn('||quote_literal(atopology)
  ||',''face'',''mbr'','||quote_literal(srid)
  ||',''POLYGON'',2)'; -- 2d only mbr is good enough

  -- Face standard view description
  EXECUTE 'COMMENT ON TABLE ' || quote_ident(atopology)
    || '.face IS '
       '''Contains face topology primitives''';

  -------------} END OF face CREATION

  --------------{ node CREATION

  EXECUTE
  'CREATE TABLE ' || quote_ident(atopology) || '.node ('
     'node_id SERIAL,'
  --|| 'geom GEOMETRY,'
     'containing_face INTEGER,'

     'CONSTRAINT node_primary_key PRIMARY KEY(node_id),'

  --|| 'CONSTRAINT node_geometry_type CHECK '
  --|| '( GeometryType(geom) = ''POINT'' ),'

     'CONSTRAINT face_exists FOREIGN KEY(containing_face) '
     'REFERENCES ' || quote_ident(atopology) || '.face(face_id)'

     ');';

  -- Add geometry column to the node table
  EXECUTE
  'SELECT AddGeometryColumn('||quote_literal(atopology)
  ||',''node'',''geom'','||quote_literal(srid)
  ||',''POINT'',' || ndims || ')';

  -- Node standard view description
  EXECUTE 'COMMENT ON TABLE ' || quote_ident(atopology)
    || '.node IS '
       '''Contains node topology primitives''';

  --------------} END OF node CREATION

  --------------{ edge CREATION

  -- edge_data table
  EXECUTE
  'CREATE TABLE ' || quote_ident(atopology) || '.edge_data ('
     'edge_id SERIAL NOT NULL PRIMARY KEY,'
     'start_node INTEGER NOT NULL,'
     'end_node INTEGER NOT NULL,'
     'next_left_edge INTEGER NOT NULL,'
     'abs_next_left_edge INTEGER NOT NULL,'
     'next_right_edge INTEGER NOT NULL,'
     'abs_next_right_edge INTEGER NOT NULL,'
     'left_face INTEGER NOT NULL,'
     'right_face INTEGER NOT NULL,'
  --   'geom GEOMETRY NOT NULL,'

  --   'CONSTRAINT edge_geometry_type CHECK '
  --   '( GeometryType(geom) = ''LINESTRING'' ),'

     'CONSTRAINT start_node_exists FOREIGN KEY(start_node)'
     ' REFERENCES ' || quote_ident(atopology) || '.node(node_id),'

     'CONSTRAINT end_node_exists FOREIGN KEY(end_node) '
     ' REFERENCES ' || quote_ident(atopology) || '.node(node_id),'

     'CONSTRAINT left_face_exists FOREIGN KEY(left_face) '
     'REFERENCES ' || quote_ident(atopology) || '.face(face_id),'

     'CONSTRAINT right_face_exists FOREIGN KEY(right_face) '
     'REFERENCES ' || quote_ident(atopology) || '.face(face_id),'

     'CONSTRAINT next_left_edge_exists FOREIGN KEY(abs_next_left_edge)'
     ' REFERENCES ' || quote_ident(atopology)
  || '.edge_data(edge_id)'
     ' DEFERRABLE INITIALLY DEFERRED,'

     'CONSTRAINT next_right_edge_exists '
     'FOREIGN KEY(abs_next_right_edge)'
     ' REFERENCES ' || quote_ident(atopology)
  || '.edge_data(edge_id) '
     ' DEFERRABLE INITIALLY DEFERRED'
     ');';

  -- Add geometry column to the edge_data table
  EXECUTE
  'SELECT AddGeometryColumn('||quote_literal(atopology)
  ||',''edge_data'',''geom'','||quote_literal(srid)
  ||',''LINESTRING'',' || ndims || ')';

  -- edge standard view (select rule)
  EXECUTE 'CREATE VIEW ' || quote_ident(atopology)
    || '.edge AS SELECT '
       ' edge_id, start_node, end_node, next_left_edge, '
       ' next_right_edge, '
       ' left_face, right_face, geom FROM '
    || quote_ident(atopology) || '.edge_data';

  -- Edge standard view description
  EXECUTE 'COMMENT ON VIEW ' || quote_ident(atopology)
    || '.edge IS '
       '''Contains edge topology primitives''';
  EXECUTE 'COMMENT ON COLUMN ' || quote_ident(atopology)
    || '.edge.edge_id IS '
       '''Unique identifier of the edge''';
  EXECUTE 'COMMENT ON COLUMN ' || quote_ident(atopology)
    || '.edge.start_node IS '
       '''Unique identifier of the node at the start of the edge''';
  EXECUTE 'COMMENT ON COLUMN ' || quote_ident(atopology)
    || '.edge.end_node IS '
       '''Unique identifier of the node at the end of the edge''';
  EXECUTE 'COMMENT ON COLUMN ' || quote_ident(atopology)
    || '.edge.next_left_edge IS '
       '''Unique identifier of the next edge of the face on the left (when looking in the direction from START_NODE to END_NODE), moving counterclockwise around the face boundary''';
  EXECUTE 'COMMENT ON COLUMN ' || quote_ident(atopology)
    || '.edge.next_right_edge IS '
       '''Unique identifier of the next edge of the face on the right (when looking in the direction from START_NODE to END_NODE), moving counterclockwise around the face boundary''';
  EXECUTE 'COMMENT ON COLUMN ' || quote_ident(atopology)
    || '.edge.left_face IS '
       '''Unique identifier of the face on the left side of the edge when looking in the direction from START_NODE to END_NODE''';
  EXECUTE 'COMMENT ON COLUMN ' || quote_ident(atopology)
    || '.edge.right_face IS '
       '''Unique identifier of the face on the right side of the edge when looking in the direction from START_NODE to END_NODE''';
  EXECUTE 'COMMENT ON COLUMN ' || quote_ident(atopology)
    || '.edge.geom IS '
       '''The geometry of the edge''';

  -- edge standard view (insert rule)
  EXECUTE 'CREATE RULE edge_insert_rule AS ON INSERT '
             'TO ' || quote_ident(atopology)
    || '.edge DO INSTEAD '
                   ' INSERT into ' || quote_ident(atopology)
    || '.edge_data '
                   ' VALUES (NEW.edge_id, NEW.start_node, NEW.end_node, '
       ' NEW.next_left_edge, abs(NEW.next_left_edge), '
       ' NEW.next_right_edge, abs(NEW.next_right_edge), '
       ' NEW.left_face, NEW.right_face, NEW.geom);';

  --------------} END OF edge CREATION

  --------------{ layer sequence
  EXECUTE 'CREATE SEQUENCE '
    || quote_ident(atopology) || '.layer_id_seq;';
  --------------} layer sequence

  --------------{ relation CREATION
  --
  EXECUTE
  'CREATE TABLE ' || quote_ident(atopology) || '.relation ('
     ' topogeo_id integer NOT NULL, '
     ' layer_id integer NOT NULL, '
     ' element_id integer NOT NULL, '
     ' element_type integer NOT NULL, '
     ' UNIQUE(layer_id,topogeo_id,element_id,element_type));';

  EXECUTE
  'CREATE TRIGGER relation_integrity_checks '
     'BEFORE UPDATE OR INSERT ON '
  || quote_ident(atopology) || '.relation FOR EACH ROW '
     ' EXECUTE PROCEDURE topology.RelationTrigger('
  ||topology_id||','||quote_literal(atopology)||')';
  --------------} END OF relation CREATION

  ------- Default (world) face
  EXECUTE 'INSERT INTO ' || quote_ident(atopology) || '.face(face_id) VALUES(0);';

  ------- GiST index on face
  EXECUTE 'CREATE INDEX face_gist ON '
    || quote_ident(atopology)
    || '.face using gist (mbr);';

  ------- GiST index on node
  EXECUTE 'CREATE INDEX node_gist ON '
    || quote_ident(atopology)
    || '.node using gist (geom);';

  ------- GiST index on edge
  EXECUTE 'CREATE INDEX edge_gist ON '
    || quote_ident(atopology)
    || '.edge_data using gist (geom);';

  ------- Indexes on left_face and right_face of edge_data
  ------- NOTE: these indexes speed up GetFaceGeometry (and thus
  -------       TopoGeometry::Geometry) by a factor of 10 !
  -------       See http://trac.osgeo.org/postgis/ticket/806
  EXECUTE 'CREATE INDEX edge_left_face_idx ON '
    || quote_ident(atopology)
    || '.edge_data (left_face);';
  EXECUTE 'CREATE INDEX edge_right_face_idx ON '
    || quote_ident(atopology)
    || '.edge_data (right_face);';

  ------- Indexes on start_node and end_node of edge_data
  ------- NOTE: this indexes speed up node deletion
  -------       by a factor of 1000 !
  -------       See http://trac.osgeo.org/postgis/ticket/2082
  EXECUTE 'CREATE INDEX edge_start_node_idx ON '
    || quote_ident(atopology)
    || '.edge_data (start_node);';
  EXECUTE 'CREATE INDEX edge_end_node_idx ON '
    || quote_ident(atopology)
    || '.edge_data (end_node);';

  -- TODO: consider also adding an index on node.containing_face

  ------- Add record to the "topology" metadata table
  EXECUTE 'INSERT INTO topology.topology '
    || '(id, name, srid, precision, hasZ) VALUES ('
    || quote_literal(topology_id) || ','
    || quote_literal(atopology) || ','
    || quote_literal(srid) || ',' || quote_literal(prec)
    || ',' || hasZ
    || ')';

  RETURN topology_id;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.createtopology(toponame character varying, srid integer, prec double precision)
 RETURNS integer
 LANGUAGE sql
 STRICT
AS $function$ SELECT topology.CreateTopology($1, $2, $3, false);$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.createtopology(character varying, integer)
 RETURNS integer
 LANGUAGE sql
 STRICT
AS $function$ SELECT topology.CreateTopology($1, $2, 0); $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.droptopogeometrycolumn(schema character varying, tbl character varying, col character varying)
 RETURNS text
 LANGUAGE plpgsql
AS $function$
DECLARE
  rec RECORD;
  lyrinfo RECORD;
  ok BOOL;
  result text;
BEGIN

        -- Get layer and topology info
  ok = false;
  FOR rec IN EXECUTE 'SELECT t.name as toponame, l.* FROM '
       'topology.topology t, topology.layer l '
       ' WHERE l.topology_id = t.id'
       ' AND l.schema_name = ' || quote_literal(schema)
    || ' AND l.table_name = ' || quote_literal(tbl)
    || ' AND l.feature_column = ' || quote_literal(col)
  LOOP
    ok = true;
    lyrinfo = rec;
  END LOOP;

  -- Layer not found
  IF NOT ok THEN
    RAISE EXCEPTION 'No layer registered on %.%.%',
      schema,tbl,col;
  END IF;

  -- Clean up the topology schema
  BEGIN
    -- Cleanup the relation table
    EXECUTE 'DELETE FROM ' || quote_ident(lyrinfo.toponame)
      || '.relation '
         ' WHERE '
         'layer_id = ' || lyrinfo.layer_id;

    -- Drop the sequence for topogeoms in this layer
    EXECUTE 'DROP SEQUENCE ' || quote_ident(lyrinfo.toponame)
      || '.topogeo_s_' || lyrinfo.layer_id;
  EXCEPTION
    WHEN UNDEFINED_TABLE THEN
      RAISE NOTICE '%', SQLERRM;
    WHEN OTHERS THEN
      RAISE EXCEPTION 'Got % (%)', SQLERRM, SQLSTATE;
  END;

  ok = false;
  FOR rec IN SELECT * FROM pg_namespace n, pg_class c, pg_attribute a
    WHERE text(n.nspname) = schema
    AND c.relnamespace = n.oid
    AND text(c.relname) = tbl
    AND a.attrelid = c.oid
    AND text(a.attname) = col
  LOOP
    ok = true;
    EXIT;
  END LOOP;

  IF ok THEN
    -- Set feature column to NULL to bypass referential integrity
    -- checks
    EXECUTE 'UPDATE ' || quote_ident(schema) || '.'
      || quote_ident(tbl)
      || ' SET ' || quote_ident(col)
      || ' = NULL';
  END IF;

  -- Delete the layer record
  EXECUTE 'DELETE FROM topology.layer '
       ' WHERE topology_id = ' || lyrinfo.topology_id
    || ' AND layer_id = ' || lyrinfo.layer_id;

  IF ok THEN
    -- Drop the layer column
    EXECUTE 'ALTER TABLE ' || quote_ident(schema) || '.'
      || quote_ident(tbl)
      || ' DROP ' || quote_ident(col)
      || ' cascade';
  END IF;

  result = 'Layer ' || lyrinfo.layer_id || ' ('
    || schema || '.' || tbl || '.' || col
    || ') dropped';

  RETURN result;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.droptopology(atopology character varying)
 RETURNS text
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
  topoid integer;
  rec RECORD;
BEGIN
  -- Get topology id
  SELECT id INTO topoid
    FROM topology.topology WHERE name = atopology;

  IF NOT FOUND THEN
    RAISE EXCEPTION 'Topology % does not exist', quote_literal(atopology);
  END IF;

  RAISE NOTICE 'Dropping all layers from topology % (%)',
    quote_literal(atopology), topoid;

  -- Drop all layers in the topology
  FOR rec IN EXECUTE 'SELECT * FROM topology.layer WHERE '
    || ' topology_id = ' || topoid
  LOOP

    EXECUTE 'SELECT topology.DropTopoGeometryColumn('
      || quote_literal(rec.schema_name)
      || ','
      || quote_literal(rec.table_name)
      || ','
      || quote_literal(rec.feature_column)
      || ')';
  END LOOP;

  -- Delete record from topology.topology
  EXECUTE 'DELETE FROM topology.topology WHERE id = '
    || topoid;

  -- Drop the schema (if it exists)
  FOR rec IN SELECT * FROM pg_namespace WHERE text(nspname) = atopology
  LOOP
    EXECUTE 'DROP SCHEMA '||quote_ident(atopology)||' CASCADE';
  END LOOP;

  RETURN 'Topology ' || quote_literal(atopology) || ' dropped';
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.equals(tg1 topology.topogeometry, tg2 topology.topogeometry)
 RETURNS boolean
 LANGUAGE plpgsql
 STABLE STRICT
AS $function$
DECLARE
  rec RECORD;
  toponame varchar;
  query text;
BEGIN

  IF tg1.topology_id != tg2.topology_id THEN
    -- TODO: revert to ::geometry instead ?
    RAISE EXCEPTION 'Cannot compare TopoGeometries from different topologies';
  END IF;

  -- Not the same type, not equal
  IF tg1.type != tg2.type THEN
    RETURN FALSE;
  END IF;

  -- Geometry collection are not currently supported
  IF tg2.type = 4 THEN
    RAISE EXCEPTION 'GeometryCollection are not supported by equals()';
  END IF;

        -- Get topology name
        SELECT name FROM topology.topology into toponame
                WHERE id = tg1.topology_id;

  -- Two geometries are equal if they are composed by
  -- the same TopoElements
  FOR rec IN EXECUTE 'SELECT * FROM '
    || ' topology.GetTopoGeomElements('
    || quote_literal(toponame) || ', '
    || tg1.layer_id || ',' || tg1.id || ') '
    || ' EXCEPT SELECT * FROM '
    || ' topology.GetTopogeomElements('
    || quote_literal(toponame) || ', '
    || tg2.layer_id || ',' || tg2.id || ');'
  LOOP
    RETURN FALSE;
  END LOOP;

  FOR rec IN EXECUTE 'SELECT * FROM '
    || ' topology.GetTopoGeomElements('
    || quote_literal(toponame) || ', '
    || tg2.layer_id || ',' || tg2.id || ')'
    || ' EXCEPT SELECT * FROM '
    || ' topology.GetTopogeomElements('
    || quote_literal(toponame) || ', '
    || tg1.layer_id || ',' || tg1.id || '); '
  LOOP
    RETURN FALSE;
  END LOOP;
  RETURN TRUE;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.geometry(topogeom topology.topogeometry)
 RETURNS geometry
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
  toponame varchar;
  geom geometry;
  rec RECORD;
  plyr RECORD;
  clyr RECORD;
  sql TEXT;
BEGIN

  -- Get topology name
  SELECT name FROM topology.topology
  WHERE id = topogeom.topology_id
  INTO toponame;
  IF toponame IS NULL THEN
    RAISE EXCEPTION 'Invalid TopoGeometry (unexistent topology id %)', topogeom.topology_id;
  END IF;

  -- Get layer info
  SELECT * FROM topology.layer
    WHERE topology_id = topogeom.topology_id
    AND layer_id = topogeom.layer_id
    INTO plyr;
  IF plyr IS NULL THEN
    RAISE EXCEPTION 'Could not find TopoGeometry layer % in topology %', topogeom.layer_id, topogeom.topology_id;
  END IF;

  --
  -- If this feature layer is on any level > 0 we will
  -- compute the topological union of all child features
  -- in fact recursing.
  --
  IF plyr.level > 0 THEN -- {

    -- Get child layer info
    SELECT * FROM topology.layer WHERE layer_id = plyr.child_id
      AND topology_id = topogeom.topology_id
      INTO clyr;
    IF clyr IS NULL THEN
      RAISE EXCEPTION 'Invalid layer % in topology % (unexistent child layer %)', topogeom.layer_id, topogeom.topology_id, plyr.child_id;
    END IF;

    sql := 'SELECT st_multi(st_union(topology.Geometry('
      || quote_ident(clyr.feature_column)
      || '))) as geom FROM '
      || quote_ident(clyr.schema_name) || '.'
      || quote_ident(clyr.table_name)
      || ', ' || quote_ident(toponame) || '.relation pr'
         ' WHERE '
         ' pr.topogeo_id = ' || topogeom.id
      || ' AND '
         ' pr.layer_id = ' || topogeom.layer_id
      || ' AND '
         ' id('||quote_ident(clyr.feature_column)
      || ') = pr.element_id '
         ' AND '
         'layer_id('||quote_ident(clyr.feature_column)
      || ') = pr.element_type ';
    --RAISE DEBUG '%', query;
    EXECUTE sql INTO geom;

  ELSIF topogeom.type = 3 THEN -- [multi]polygon -- }{

    sql := 'SELECT st_multi(st_union('
         'topology.ST_GetFaceGeometry('
      || quote_literal(toponame) || ','
      || 'element_id))) as g FROM '
      || quote_ident(toponame)
      || '.relation WHERE topogeo_id = '
      || topogeom.id || ' AND layer_id = '
      || topogeom.layer_id || ' AND element_type = 3 ';
    EXECUTE sql INTO geom;

  ELSIF topogeom.type = 2 THEN -- [multi]line -- }{

    sql :=
      'SELECT st_multi(ST_LineMerge(ST_Collect(e.geom))) as g FROM '
      || quote_ident(toponame) || '.edge e, '
      || quote_ident(toponame) || '.relation r '
         ' WHERE r.topogeo_id = ' || topogeom.id
      || ' AND r.layer_id = ' || topogeom.layer_id
      || ' AND r.element_type = 2 '
         ' AND abs(r.element_id) = e.edge_id';
    EXECUTE sql INTO geom;

  ELSIF topogeom.type = 1 THEN -- [multi]point -- }{

    sql :=
      'SELECT st_multi(st_union(n.geom)) as g FROM '
      || quote_ident(toponame) || '.node n, '
      || quote_ident(toponame) || '.relation r '
         ' WHERE r.topogeo_id = ' || topogeom.id
      || ' AND r.layer_id = ' || topogeom.layer_id
      || ' AND r.element_type = 1 '
         ' AND r.element_id = n.node_id';
    EXECUTE sql INTO geom;

  ELSIF topogeom.type = 4 THEN -- mixed collection -- }{

    sql := 'WITH areas AS ( SELECT ST_Union('
         'topology.ST_GetFaceGeometry('
      || quote_literal(toponame) || ','
      || 'element_id)) as g FROM '
      || quote_ident(toponame)
      || '.relation WHERE topogeo_id = '
      || topogeom.id || ' AND layer_id = '
      || topogeom.layer_id || ' AND element_type = 3), '
         'lines AS ( SELECT ST_LineMerge(ST_Collect(e.geom)) as g FROM '
      || quote_ident(toponame) || '.edge e, '
      || quote_ident(toponame) || '.relation r '
         ' WHERE r.topogeo_id = ' || topogeom.id
      || ' AND r.layer_id = ' || topogeom.layer_id
      || ' AND r.element_type = 2 '
         ' AND abs(r.element_id) = e.edge_id ), '
         ' points as ( SELECT st_union(n.geom) as g FROM '
      || quote_ident(toponame) || '.node n, '
      || quote_ident(toponame) || '.relation r '
         ' WHERE r.topogeo_id = ' || topogeom.id
      || ' AND r.layer_id = ' || topogeom.layer_id
      || ' AND r.element_type = 1 '
         ' AND r.element_id = n.node_id ), '
         ' un as ( SELECT g FROM areas UNION ALL SELECT g FROM lines '
         '          UNION ALL SELECT g FROM points ) '
         'SELECT ST_Multi(ST_Collect(g)) FROM un';
    EXECUTE sql INTO geom;

  ELSE -- }{

    RAISE EXCEPTION 'Invalid TopoGeometries (unknown type %)', topogeom.type;

  END IF; -- }

  IF geom IS NULL THEN
    IF topogeom.type = 3 THEN -- [multi]polygon
      geom := 'MULTIPOLYGON EMPTY';
    ELSIF topogeom.type = 2 THEN -- [multi]line
      geom := 'MULTILINESTRING EMPTY';
    ELSIF topogeom.type = 1 THEN -- [multi]point
      geom := 'MULTIPOINT EMPTY';
    ELSE
      geom := 'GEOMETRYCOLLECTION EMPTY';
    END IF;
  END IF;

  RETURN geom;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.geometrytype(tg topology.topogeometry)
 RETURNS text
 LANGUAGE sql
 STABLE STRICT
AS $function$
	SELECT CASE
		WHEN type($1) = 1 THEN 'MULTIPOINT'
		WHEN type($1) = 2 THEN 'MULTILINESTRING'
		WHEN type($1) = 3 THEN 'MULTIPOLYGON'
		WHEN type($1) = 4 THEN 'GEOMETRYCOLLECTION'
		ELSE 'UNEXPECTED'
		END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.getedgebypoint(atopology character varying, apoint geometry, tol1 double precision)
 RETURNS integer
 LANGUAGE c
 STABLE STRICT
AS '$libdir/postgis_topology-2.5', $function$GetEdgeByPoint$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.getfacebypoint(atopology character varying, apoint geometry, tol1 double precision)
 RETURNS integer
 LANGUAGE c
 STABLE STRICT
AS '$libdir/postgis_topology-2.5', $function$GetFaceByPoint$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.getnodebypoint(atopology character varying, apoint geometry, tol1 double precision)
 RETURNS integer
 LANGUAGE c
 STABLE STRICT
AS '$libdir/postgis_topology-2.5', $function$GetNodeByPoint$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.getnodeedges(atopology character varying, anode integer)
 RETURNS SETOF topology.getfaceedges_returntype
 LANGUAGE plpgsql
 STABLE
AS $function$
DECLARE
  curedge int;
  nextedge int;
  rec RECORD;
  retrec topology.GetFaceEdges_ReturnType;
  n int;
  sql text;
BEGIN

  n := 0;
  sql :=
    'WITH incident_edges AS ( SELECT edge_id, start_node, end_node, ST_RemoveRepeatedPoints(geom) as geom FROM '
    || quote_ident(atopology)
    || '.edge_data WHERE start_node = ' || anode
    || ' or end_node = ' || anode
    || ') SELECT edge_id, ST_Azimuth(ST_StartPoint(geom), ST_PointN(geom, 2)) as az FROM  incident_edges WHERE start_node = ' || anode
    || ' UNION ALL SELECT -edge_id, ST_Azimuth(ST_EndPoint(geom), ST_PointN(geom, ST_NumPoints(geom)-1)) FROM incident_edges WHERE end_node = ' || anode
    || ' ORDER BY az';

  FOR rec IN EXECUTE sql
  LOOP -- incident edges {

    n := n + 1;
    retrec.sequence := n;
    retrec.edge := rec.edge_id;
    RETURN NEXT retrec;
  END LOOP; -- incident edges }

END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.getringedges(atopology character varying, anedge integer, maxedges integer DEFAULT NULL::integer)
 RETURNS SETOF topology.getfaceedges_returntype
 LANGUAGE plpgsql
 STABLE
AS $function$
DECLARE
  rec RECORD;
  retrec topology.GetFaceEdges_ReturnType;
  n int;
  sql text;
BEGIN
  sql := 'WITH RECURSIVE edgering AS ( SELECT '
    || anedge
    || ' as signed_edge_id, edge_id, next_left_edge, next_right_edge FROM '
    || quote_ident(atopology)
    || '.edge_data WHERE edge_id = '
    || abs(anedge)
    || ' UNION '
    || ' SELECT CASE WHEN p.signed_edge_id < 0 THEN p.next_right_edge '
    || ' ELSE p.next_left_edge END, e.edge_id, e.next_left_edge, e.next_right_edge '
    || ' FROM ' || quote_ident(atopology)
    || '.edge_data e, edgering p WHERE e.edge_id = CASE WHEN p.signed_edge_id < 0 '
    || 'THEN abs(p.next_right_edge) ELSE abs(p.next_left_edge) END ) SELECT * FROM edgering';

  n := 1;
  FOR rec IN EXECUTE sql
  LOOP
    retrec.sequence := n;
    retrec.edge := rec.signed_edge_id;
    RETURN NEXT retrec;

    n := n + 1;

    IF n > maxedges THEN
      RAISE EXCEPTION 'Max traversing limit hit: %', maxedges;
    END IF;
  END LOOP;

END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.gettopogeomelementarray(toponame character varying, layer_id integer, tgid integer)
 RETURNS topology.topoelementarray
 LANGUAGE plpgsql
 STABLE STRICT
AS $function$
DECLARE
  rec RECORD;
  tg_objs varchar := '{';
  i integer;
  query text;
BEGIN

  query = 'SELECT * FROM topology.GetTopoGeomElements('
    || quote_literal(toponame) || ','
    || quote_literal(layer_id) || ','
    || quote_literal(tgid)
    || ') as obj ORDER BY obj';


  -- TODO: why not using array_agg here ?

  i = 1;
  FOR rec IN EXECUTE query
  LOOP
    IF i > 1 THEN
      tg_objs = tg_objs || ',';
    END IF;
    tg_objs = tg_objs || '{'
      || rec.obj[1] || ',' || rec.obj[2]
      || '}';
    i = i+1;
  END LOOP;

  tg_objs = tg_objs || '}';

  RETURN tg_objs;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.gettopogeomelementarray(tg topology.topogeometry)
 RETURNS topology.topoelementarray
 LANGUAGE plpgsql
 STABLE STRICT
AS $function$
DECLARE
  toponame varchar;
BEGIN
  toponame = topology.GetTopologyName(tg.topology_id);
  RETURN topology.GetTopoGeomElementArray(toponame, tg.layer_id, tg.id);
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.gettopogeomelements(toponame character varying, layerid integer, tgid integer)
 RETURNS SETOF topology.topoelement
 LANGUAGE plpgsql
 STABLE STRICT
AS $function$
DECLARE
  ret topology.TopoElement;
  rec RECORD;
  rec2 RECORD;
  query text;
  query2 text;
  lyr RECORD;
  ok bool;
  topoid INTEGER;
BEGIN

  -- Get topology id
  SELECT id INTO topoid
    FROM topology.topology WHERE name = toponame;
  IF NOT FOUND THEN
    RAISE EXCEPTION 'Topology % does not exist', quote_literal(toponame);
  END IF;

  -- Get layer info
  ok = false;
  FOR rec IN EXECUTE 'SELECT * FROM topology.layer '
       ' WHERE layer_id = $1 AND topology_id = $2'
       USING layerid, topoid
  LOOP
    lyr = rec;
    ok = true;
  END LOOP;

  IF NOT ok THEN
    RAISE EXCEPTION 'Layer % does not exist', layerid;
  END IF;

  query = 'SELECT abs(element_id) as element_id, element_type FROM '
    || quote_ident(toponame) || '.relation WHERE '
       ' layer_id = ' || layerid
    || ' AND topogeo_id = ' || quote_literal(tgid)
    || ' ORDER BY element_type, element_id';

  --RAISE NOTICE 'Query: %', query;

  FOR rec IN EXECUTE query
  LOOP
    IF lyr.level > 0 THEN
      query2 = 'SELECT * from topology.GetTopoGeomElements('
        || quote_literal(toponame) || ','
        || rec.element_type
        || ','
        || rec.element_id
        || ') as ret;';
      --RAISE NOTICE 'Query2: %', query2;
      FOR rec2 IN EXECUTE query2
      LOOP
        RETURN NEXT rec2.ret;
      END LOOP;
    ELSE
      ret = '{' || rec.element_id || ',' || rec.element_type || '}';
      RETURN NEXT ret;
    END IF;

  END LOOP;

  RETURN;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.gettopogeomelements(tg topology.topogeometry)
 RETURNS SETOF topology.topoelement
 LANGUAGE plpgsql
 STABLE STRICT
AS $function$
DECLARE
  toponame varchar;
  rec RECORD;
BEGIN
  toponame = topology.GetTopologyName(tg.topology_id);
  FOR rec IN SELECT * FROM topology.GetTopoGeomElements(toponame,
    tg.layer_id,tg.id) as ret
  LOOP
    RETURN NEXT rec.ret;
  END LOOP;
  RETURN;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.gettopologyid(toponame character varying)
 RETURNS integer
 LANGUAGE plpgsql
 STABLE STRICT
AS $function$
DECLARE
  ret integer;
BEGIN
  SELECT id INTO ret
    FROM topology.topology WHERE name = toponame;

  IF NOT FOUND THEN
    RAISE EXCEPTION 'Topology % does not exist', quote_literal(toponame);
  END IF;

  RETURN ret;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.gettopologyname(topoid integer)
 RETURNS character varying
 LANGUAGE plpgsql
 STABLE STRICT
AS $function$
DECLARE
  ret varchar;
BEGIN
        SELECT name FROM topology.topology into ret
                WHERE id = topoid;
  RETURN ret;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.gettopologysrid(toponame character varying)
 RETURNS integer
 LANGUAGE sql
 STABLE STRICT
AS $function$
  SELECT SRID FROM topology.topology WHERE name = $1;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.intersects(tg1 topology.topogeometry, tg2 topology.topogeometry)
 RETURNS boolean
 LANGUAGE plpgsql
 STABLE STRICT
AS $function$
DECLARE
  tgbuf topology.TopoGeometry;
  rec RECORD;
  toponame varchar;
  query text;
BEGIN
  IF tg1.topology_id != tg2.topology_id THEN
    -- TODO: revert to ::geometry instead ?
    RAISE EXCEPTION 'Cannot compute intersection between TopoGeometries from different topologies';
  END IF;

  -- Order TopoGeometries so that tg1 has less-or-same
  -- dimensionality of tg1 (point,line,polygon,collection)
  IF tg1.type > tg2.type THEN
    tgbuf := tg2;
    tg2 := tg1;
    tg1 := tgbuf;
  END IF;

  --RAISE NOTICE 'tg1.id:% tg2.id:%', tg1.id, tg2.id;
  -- Geometry collection are not currently supported
  IF tg2.type = 4 THEN
    RAISE EXCEPTION 'GeometryCollection are not supported by intersects()';
  END IF;

        -- Get topology name
        SELECT name FROM topology.topology into toponame
                WHERE id = tg1.topology_id;

  -- Hierarchical TopoGeometries are not currently supported
  query = 'SELECT level FROM topology.layer'
    || ' WHERE '
    || ' topology_id = ' || tg1.topology_id
    || ' AND '
    || '( layer_id = ' || tg1.layer_id
    || ' OR layer_id = ' || tg2.layer_id
    || ' ) '
    || ' AND level > 0 ';

  --RAISE NOTICE '%', query;

  FOR rec IN EXECUTE query
  LOOP
    -- TODO: revert to ::geometry instead ?
    RAISE EXCEPTION 'Hierarchical TopoGeometries are not currently supported by intersects()';
  END LOOP;

  IF tg1.type = 1 THEN -- [multi]point

    IF tg2.type = 1 THEN -- point/point
  ---------------------------------------------------------
  --
  --  Two [multi]point features intersect if they share
  --  any Node
  --
  --
  --
      query =
        'SELECT a.topogeo_id FROM '
        || quote_ident(toponame) ||
        '.relation a, '
        || quote_ident(toponame) ||
        '.relation b '
        || 'WHERE a.layer_id = ' || tg1.layer_id
        || ' AND b.layer_id = ' || tg2.layer_id
        || ' AND a.topogeo_id = ' || tg1.id
        || ' AND b.topogeo_id = ' || tg2.id
        || ' AND a.element_id = b.element_id '
        || ' LIMIT 1';
      --RAISE NOTICE '%', query;
      FOR rec IN EXECUTE query
      LOOP
        RETURN TRUE; -- they share an element
      END LOOP;
      RETURN FALSE; -- no elements shared
  --
  ---------------------------------------------------------

    ELSIF tg2.type = 2 THEN -- point/line
  ---------------------------------------------------------
  --
  --  A [multi]point intersects a [multi]line if they share
  --  any Node.
  --
  --
  --
      query =
        'SELECT a.topogeo_id FROM '
        || quote_ident(toponame) ||
        '.relation a, '
        || quote_ident(toponame) ||
        '.relation b, '
        || quote_ident(toponame) ||
        '.edge_data e '
        || 'WHERE a.layer_id = ' || tg1.layer_id
        || ' AND b.layer_id = ' || tg2.layer_id
        || ' AND a.topogeo_id = ' || tg1.id
        || ' AND b.topogeo_id = ' || tg2.id
        || ' AND abs(b.element_id) = e.edge_id '
        || ' AND ( '
          || ' e.start_node = a.element_id '
          || ' OR '
          || ' e.end_node = a.element_id '
        || ' )'
        || ' LIMIT 1';
      --RAISE NOTICE '%', query;
      FOR rec IN EXECUTE query
      LOOP
        RETURN TRUE; -- they share an element
      END LOOP;
      RETURN FALSE; -- no elements shared
  --
  ---------------------------------------------------------

    ELSIF tg2.type = 3 THEN -- point/polygon
  ---------------------------------------------------------
  --
  --  A [multi]point intersects a [multi]polygon if any
  --  Node of the point is contained in any face of the
  --  polygon OR ( is end_node or start_node of any edge
  --  of any polygon face ).
  --
  --  We assume the Node-in-Face check is faster becasue
  --  there will be less Faces then Edges in any polygon.
  --
  --
  --
  --
      -- Check if any node is contained in a face
      query =
        'SELECT n.node_id as id FROM '
        || quote_ident(toponame) ||
        '.relation r1, '
        || quote_ident(toponame) ||
        '.relation r2, '
        || quote_ident(toponame) ||
        '.node n '
        || 'WHERE r1.layer_id = ' || tg1.layer_id
        || ' AND r2.layer_id = ' || tg2.layer_id
        || ' AND r1.topogeo_id = ' || tg1.id
        || ' AND r2.topogeo_id = ' || tg2.id
        || ' AND n.node_id = r1.element_id '
        || ' AND r2.element_id = n.containing_face '
        || ' LIMIT 1';
      --RAISE NOTICE '%', query;
      FOR rec IN EXECUTE query
      LOOP
        --RAISE NOTICE 'Node % in polygon face', rec.id;
        RETURN TRUE; -- one (or more) nodes are
                     -- contained in a polygon face
      END LOOP;

      -- Check if any node is start or end of any polygon
      -- face edge
      query =
        'SELECT n.node_id as nid, e.edge_id as eid '
        || ' FROM '
        || quote_ident(toponame) ||
        '.relation r1, '
        || quote_ident(toponame) ||
        '.relation r2, '
        || quote_ident(toponame) ||
        '.edge_data e, '
        || quote_ident(toponame) ||
        '.node n '
        || 'WHERE r1.layer_id = ' || tg1.layer_id
        || ' AND r2.layer_id = ' || tg2.layer_id
        || ' AND r1.topogeo_id = ' || tg1.id
        || ' AND r2.topogeo_id = ' || tg2.id
        || ' AND n.node_id = r1.element_id '
        || ' AND ( '
        || ' e.left_face = r2.element_id '
        || ' OR '
        || ' e.right_face = r2.element_id '
        || ' ) '
        || ' AND ( '
        || ' e.start_node = r1.element_id '
        || ' OR '
        || ' e.end_node = r1.element_id '
        || ' ) '
        || ' LIMIT 1';
      --RAISE NOTICE '%', query;
      FOR rec IN EXECUTE query
      LOOP
        --RAISE NOTICE 'Node % on edge % bound', rec.nid, rec.eid;
        RETURN TRUE; -- one node is start or end
                     -- of a face edge
      END LOOP;

      RETURN FALSE; -- no intersection
  --
  ---------------------------------------------------------

    ELSIF tg2.type = 4 THEN -- point/collection
      RAISE EXCEPTION 'Intersection point/collection not implemented yet';

    ELSE
      RAISE EXCEPTION 'Invalid TopoGeometry type %', tg2.type;
    END IF;

  ELSIF tg1.type = 2 THEN -- [multi]line
    IF tg2.type = 2 THEN -- line/line
  ---------------------------------------------------------
  --
  --  A [multi]line intersects a [multi]line if they share
  --  any Node.
  --
  --
  --
      query =
        'SELECT e1.start_node FROM '
        || quote_ident(toponame) ||
        '.relation r1, '
        || quote_ident(toponame) ||
        '.relation r2, '
        || quote_ident(toponame) ||
        '.edge_data e1, '
        || quote_ident(toponame) ||
        '.edge_data e2 '
        || 'WHERE r1.layer_id = ' || tg1.layer_id
        || ' AND r2.layer_id = ' || tg2.layer_id
        || ' AND r1.topogeo_id = ' || tg1.id
        || ' AND r2.topogeo_id = ' || tg2.id
        || ' AND abs(r1.element_id) = e1.edge_id '
        || ' AND abs(r2.element_id) = e2.edge_id '
        || ' AND ( '
        || ' e1.start_node = e2.start_node '
        || ' OR '
        || ' e1.start_node = e2.end_node '
        || ' OR '
        || ' e1.end_node = e2.start_node '
        || ' OR '
        || ' e1.end_node = e2.end_node '
        || ' )'
        || ' LIMIT 1';
      --RAISE NOTICE '%', query;
      FOR rec IN EXECUTE query
      LOOP
        RETURN TRUE; -- they share an element
      END LOOP;
      RETURN FALSE; -- no elements shared
  --
  ---------------------------------------------------------

    ELSIF tg2.type = 3 THEN -- line/polygon
  ---------------------------------------------------------
  --
  -- A [multi]line intersects a [multi]polygon if they share
  -- any Node (touch-only case), or if any line edge has any
  -- polygon face on the left or right (full-containment case
  -- + edge crossing case).
  --
  --
      -- E1 are line edges, E2 are polygon edges
      -- R1 are line relations.
      -- R2 are polygon relations.
      -- R2.element_id are FACE ids
      query =
        'SELECT e1.edge_id'
        || ' FROM '
        || quote_ident(toponame) ||
        '.relation r1, '
        || quote_ident(toponame) ||
        '.relation r2, '
        || quote_ident(toponame) ||
        '.edge_data e1, '
        || quote_ident(toponame) ||
        '.edge_data e2 '
        || 'WHERE r1.layer_id = ' || tg1.layer_id
        || ' AND r2.layer_id = ' || tg2.layer_id
        || ' AND r1.topogeo_id = ' || tg1.id
        || ' AND r2.topogeo_id = ' || tg2.id

        -- E1 are line edges
        || ' AND e1.edge_id = abs(r1.element_id) '

        -- E2 are face edges
        || ' AND ( e2.left_face = r2.element_id '
        || '   OR e2.right_face = r2.element_id ) '

        || ' AND ( '

        -- Check if E1 have left-or-right face
        -- being part of R2.element_id
        || ' e1.left_face = r2.element_id '
        || ' OR '
        || ' e1.right_face = r2.element_id '

        -- Check if E1 share start-or-end node
        -- with any E2.
        || ' OR '
        || ' e1.start_node = e2.start_node '
        || ' OR '
        || ' e1.start_node = e2.end_node '
        || ' OR '
        || ' e1.end_node = e2.start_node '
        || ' OR '
        || ' e1.end_node = e2.end_node '

        || ' ) '

        || ' LIMIT 1';
      --RAISE NOTICE '%', query;
      FOR rec IN EXECUTE query
      LOOP
        RETURN TRUE; -- either common node
                     -- or edge-in-face
      END LOOP;

      RETURN FALSE; -- no intersection
  --
  ---------------------------------------------------------

    ELSIF tg2.type = 4 THEN -- line/collection
      RAISE EXCEPTION 'Intersection line/collection not implemented yet';

    ELSE
      RAISE EXCEPTION 'Invalid TopoGeometry type %', tg2.type;
    END IF;

  ELSIF tg1.type = 3 THEN -- [multi]polygon

    IF tg2.type = 3 THEN -- polygon/polygon
  ---------------------------------------------------------
  --
  -- A [multi]polygon intersects a [multi]polygon if they share
  -- any Node (touch-only case), or if any face edge has any of the
  -- other polygon face on the left or right (full-containment case
  -- + edge crossing case).
  --
  --
      -- E1 are poly1 edges.
      -- E2 are poly2 edges
      -- R1 are poly1 relations.
      -- R2 are poly2 relations.
      -- R1.element_id are poly1 FACE ids
      -- R2.element_id are poly2 FACE ids
      query =
        'SELECT e1.edge_id'
        || ' FROM '
        || quote_ident(toponame) ||
        '.relation r1, '
        || quote_ident(toponame) ||
        '.relation r2, '
        || quote_ident(toponame) ||
        '.edge_data e1, '
        || quote_ident(toponame) ||
        '.edge_data e2 '
        || 'WHERE r1.layer_id = ' || tg1.layer_id
        || ' AND r2.layer_id = ' || tg2.layer_id
        || ' AND r1.topogeo_id = ' || tg1.id
        || ' AND r2.topogeo_id = ' || tg2.id

        -- E1 are poly1 edges
        || ' AND ( e1.left_face = r1.element_id '
        || '   OR e1.right_face = r1.element_id ) '

        -- E2 are poly2 edges
        || ' AND ( e2.left_face = r2.element_id '
        || '   OR e2.right_face = r2.element_id ) '

        || ' AND ( '

        -- Check if any edge from a polygon face
        -- has any of the other polygon face
        -- on the left or right
        || ' e1.left_face = r2.element_id '
        || ' OR '
        || ' e1.right_face = r2.element_id '
        || ' OR '
        || ' e2.left_face = r1.element_id '
        || ' OR '
        || ' e2.right_face = r1.element_id '

        -- Check if E1 share start-or-end node
        -- with any E2.
        || ' OR '
        || ' e1.start_node = e2.start_node '
        || ' OR '
        || ' e1.start_node = e2.end_node '
        || ' OR '
        || ' e1.end_node = e2.start_node '
        || ' OR '
        || ' e1.end_node = e2.end_node '

        || ' ) '

        || ' LIMIT 1';
      --RAISE NOTICE '%', query;
      FOR rec IN EXECUTE query
      LOOP
        RETURN TRUE; -- either common node
                     -- or edge-in-face
      END LOOP;

      RETURN FALSE; -- no intersection
  --
  ---------------------------------------------------------

    ELSIF tg2.type = 4 THEN -- polygon/collection
      RAISE EXCEPTION 'Intersection poly/collection not implemented yet';

    ELSE
      RAISE EXCEPTION 'Invalid TopoGeometry type %', tg2.type;
    END IF;

  ELSIF tg1.type = 4 THEN -- collection
    IF tg2.type = 4 THEN -- collection/collection
      RAISE EXCEPTION 'Intersection collection/collection not implemented yet';
    ELSE
      RAISE EXCEPTION 'Invalid TopoGeometry type %', tg2.type;
    END IF;

  ELSE
    RAISE EXCEPTION 'Invalid TopoGeometry type %', tg1.type;
  END IF;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.layertrigger()
 RETURNS trigger
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
  rec RECORD;
  ok BOOL;
  toponame varchar;
  query TEXT;
BEGIN

  --RAISE NOTICE 'LayerTrigger called % % at % level', TG_WHEN, TG_OP, TG_LEVEL;

  IF TG_OP = 'INSERT' THEN
    RAISE EXCEPTION 'LayerTrigger not meant to be called on INSERT';
  ELSIF TG_OP = 'UPDATE' THEN
    RAISE EXCEPTION 'The topology.layer table cannot be updated';
  END IF;

  -- Check for existance of any feature column referencing
  -- this layer
  FOR rec IN SELECT * FROM pg_namespace n, pg_class c, pg_attribute a
    WHERE text(n.nspname) = OLD.schema_name
    AND c.relnamespace = n.oid
    AND text(c.relname) = OLD.table_name
    AND a.attrelid = c.oid
    AND text(a.attname) = OLD.feature_column
  LOOP
    query = 'SELECT * '
         ' FROM ' || quote_ident(OLD.schema_name)
      || '.' || quote_ident(OLD.table_name)
      || ' WHERE layer_id('
      || quote_ident(OLD.feature_column)||') '
         '=' || OLD.layer_id
      || ' LIMIT 1';
    --RAISE NOTICE '%', query;
    FOR rec IN EXECUTE query
    LOOP
      RAISE NOTICE 'A feature referencing layer % of topology % still exists in %.%.%', OLD.layer_id, OLD.topology_id, OLD.schema_name, OLD.table_name, OLD.feature_column;
      RETURN NULL;
    END LOOP;
  END LOOP;

  -- Get topology name
  SELECT name FROM topology.topology INTO toponame
    WHERE id = OLD.topology_id;

  IF toponame IS NULL THEN
    RAISE NOTICE 'Could not find name of topology with id %',
      OLD.layer_id;
  END IF;

  -- Check if any record in the relation table references this layer
  FOR rec IN SELECT c.oid FROM pg_namespace n, pg_class c
    WHERE text(n.nspname) = toponame AND c.relnamespace = n.oid
          AND c.relname = 'relation'
  LOOP
    query = 'SELECT * '
         ' FROM ' || quote_ident(toponame)
      || '.relation '
         ' WHERE layer_id = '|| OLD.layer_id
      || ' LIMIT 1';
    --RAISE NOTICE '%', query;
    FOR rec IN EXECUTE query
    LOOP
      RAISE NOTICE 'A record in %.relation still references layer %', toponame, OLD.layer_id;
      RETURN NULL;
    END LOOP;
  END LOOP;

  RETURN OLD;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.polygonize(toponame character varying)
 RETURNS text
 LANGUAGE plpgsql
AS $function$
DECLARE
  sql text;
  rec RECORD;
  faces int;
BEGIN

  sql := 'SELECT (st_dump(st_polygonize(geom))).geom from '
         || quote_ident(toponame) || '.edge_data';

  faces = 0;
  FOR rec in EXECUTE sql LOOP
    BEGIN
      PERFORM topology.AddFace(toponame, rec.geom);
      faces = faces + 1;
    EXCEPTION
      WHEN OTHERS THEN
        RAISE WARNING 'Error registering face % (%)', rec.geom, SQLERRM;
    END;
  END LOOP;
  RETURN faces || ' faces registered';
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.populate_topology_layer()
 RETURNS TABLE(schema_name text, table_name text, feature_column text)
 LANGUAGE sql
AS $function$
  INSERT INTO topology.layer
  WITH checks AS (
  SELECT
    n.nspname sch, r.relname tab,
    replace(c.conname, 'check_topogeom_', '') col,
    --c.consrc src,
    regexp_matches(c.consrc,
      '\.topology_id = (\d+).*\.layer_id = (\d+).*\.type = (\d+)') inf
  FROM (SELECT conname, connamespace, conrelid, conkey, pg_get_constraintdef(oid) As consrc
		    FROM pg_constraint) AS c, pg_class r, pg_namespace n
  WHERE c.conname LIKE 'check_topogeom_%'
    AND r.oid = c.conrelid
    AND n.oid = r.relnamespace
  ), newrows AS (
    SELECT inf[1]::int as topology_id,
           inf[2]::int as layer_id,
          sch, tab, col, inf[3]::int as feature_type --, src
    FROM checks c
    WHERE NOT EXISTS (
      SELECT * FROM topology.layer l
      WHERE l.schema_name = c.sch
        AND l.table_name = c.tab
        AND l.feature_column = c.col
    )
  )
  SELECT topology_id, layer_id, sch,
         tab, col, feature_type,
         0, NULL
  FROM newrows RETURNING schema_name,table_name,feature_column;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.postgis_topology_scripts_installed()
 RETURNS text
 LANGUAGE sql
 IMMUTABLE
AS $function$ SELECT '2.5.5'::text || ' r' || 0::text AS version $function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.relationtrigger()
 RETURNS trigger
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
  toponame varchar;
  topoid integer;
  plyr RECORD; -- parent layer
  rec RECORD;
  ok BOOL;

BEGIN
  IF TG_NARGS != 2 THEN
    RAISE EXCEPTION 'RelationTrigger called with wrong number of arguments';
  END IF;

  topoid = TG_ARGV[0];
  toponame = TG_ARGV[1];

  --RAISE NOTICE 'RelationTrigger called % % on %.relation for a %', TG_WHEN, TG_OP, toponame, TG_LEVEL;

  IF TG_OP = 'DELETE' THEN
    RAISE EXCEPTION 'RelationTrigger not meant to be called on DELETE';
  END IF;

  -- Get layer info (and verify it exists)
  ok = false;
  FOR plyr IN EXECUTE 'SELECT * FROM topology.layer '
       'WHERE '
       ' topology_id = ' || topoid
    || ' AND'
       ' layer_id = ' || NEW.layer_id
  LOOP
    ok = true;
    EXIT;
  END LOOP;
  IF NOT ok THEN
    RAISE EXCEPTION 'Layer % does not exist in topology %',
      NEW.layer_id, topoid;
    RETURN NULL;
  END IF;

  IF plyr.level > 0 THEN -- this is hierarchical layer

    -- ElementType must be the layer child id
    IF NEW.element_type != plyr.child_id THEN
      RAISE EXCEPTION 'Type of elements in layer % must be set to its child layer id %', plyr.layer_id, plyr.child_id;
      RETURN NULL;
    END IF;

    -- ElementId must be an existent TopoGeometry in child layer
    ok = false;
    FOR rec IN EXECUTE 'SELECT topogeo_id FROM '
      || quote_ident(toponame) || '.relation '
         ' WHERE layer_id = ' || plyr.child_id
      || ' AND topogeo_id = ' || NEW.element_id
    LOOP
      ok = true;
      EXIT;
    END LOOP;
    IF NOT ok THEN
      RAISE EXCEPTION 'TopoGeometry % does not exist in the child layer %', NEW.element_id, plyr.child_id;
      RETURN NULL;
    END IF;

  ELSE -- this is a basic layer

    -- ElementType must be compatible with layer type
    IF plyr.feature_type != 4
      AND plyr.feature_type != NEW.element_type
    THEN
      RAISE EXCEPTION 'Element of type % is not compatible with layer of type %', NEW.element_type, plyr.feature_type;
      RETURN NULL;
    END IF;

    --
    -- Now lets see if the element is consistent, which
    -- is it exists in the topology tables.
    --

    --
    -- Element is a Node
    --
    IF NEW.element_type = 1
    THEN
      ok = false;
      FOR rec IN EXECUTE 'SELECT node_id FROM '
        || quote_ident(toponame) || '.node '
           ' WHERE node_id = ' || NEW.element_id
      LOOP
        ok = true;
        EXIT;
      END LOOP;
      IF NOT ok THEN
        RAISE EXCEPTION 'Node % does not exist in topology %', NEW.element_id, toponame;
        RETURN NULL;
      END IF;

    --
    -- Element is an Edge
    --
    ELSIF NEW.element_type = 2
    THEN
      ok = false;
      FOR rec IN EXECUTE 'SELECT edge_id FROM '
        || quote_ident(toponame) || '.edge_data '
           ' WHERE edge_id = ' || abs(NEW.element_id)
      LOOP
        ok = true;
        EXIT;
      END LOOP;
      IF NOT ok THEN
        RAISE EXCEPTION 'Edge % does not exist in topology %', NEW.element_id, toponame;
        RETURN NULL;
      END IF;

    --
    -- Element is a Face
    --
    ELSIF NEW.element_type = 3
    THEN
      IF NEW.element_id = 0 THEN
        RAISE EXCEPTION 'Face % cannot be associated with any feature', NEW.element_id;
        RETURN NULL;
      END IF;
      ok = false;
      FOR rec IN EXECUTE 'SELECT face_id FROM '
        || quote_ident(toponame) || '.face '
           ' WHERE face_id = ' || NEW.element_id
      LOOP
        ok = true;
        EXIT;
      END LOOP;
      IF NOT ok THEN
        RAISE EXCEPTION 'Face % does not exist in topology %', NEW.element_id, toponame;
        RETURN NULL;
      END IF;
    END IF;

  END IF;

  RETURN NEW;
END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_addedgemodface(atopology character varying, anode integer, anothernode integer, acurve geometry)
 RETURNS integer
 LANGUAGE c
AS '$libdir/postgis_topology-2.5', $function$ST_AddEdgeModFace$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_addedgenewfaces(atopology character varying, anode integer, anothernode integer, acurve geometry)
 RETURNS integer
 LANGUAGE c
AS '$libdir/postgis_topology-2.5', $function$ST_AddEdgeNewFaces$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_addisoedge(atopology character varying, anode integer, anothernode integer, acurve geometry)
 RETURNS integer
 LANGUAGE c
AS '$libdir/postgis_topology-2.5', $function$ST_AddIsoEdge$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_addisonode(atopology character varying, aface integer, apoint geometry)
 RETURNS integer
 LANGUAGE c
AS '$libdir/postgis_topology-2.5', $function$ST_AddIsoNode$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_changeedgegeom(atopology character varying, anedge integer, acurve geometry)
 RETURNS text
 LANGUAGE c
AS '$libdir/postgis_topology-2.5', $function$ST_ChangeEdgeGeom$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_createtopogeo(atopology character varying, acollection geometry)
 RETURNS text
 LANGUAGE plpgsql
AS $function$
DECLARE
  typ char(4);
  rec RECORD;
  ret int;
  nodededges GEOMETRY;
  points GEOMETRY;
  snode_id int;
  enode_id int;
  tolerance FLOAT8;
  topoinfo RECORD;
BEGIN

  IF atopology IS NULL OR acollection IS NULL THEN
    RAISE EXCEPTION 'SQL/MM Spatial exception - null argument';
  END IF;

  -- Get topology information
  BEGIN
    SELECT * FROM topology.topology
      INTO STRICT topoinfo WHERE name = atopology;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      RAISE EXCEPTION 'SQL/MM Spatial exception - invalid topology name';
  END;

  -- Check SRID compatibility
  IF ST_SRID(acollection) != topoinfo.SRID THEN
    RAISE EXCEPTION 'Geometry SRID (%) does not match topology SRID (%)',
      ST_SRID(acollection), topoinfo.SRID;
  END IF;

  -- Verify pre-conditions (valid, empty topology schema exists)
  BEGIN -- {

    -- Verify the topology views in the topology schema to be empty
    FOR rec in EXECUTE
      'SELECT count(*) FROM '
      || quote_ident(atopology) || '.edge_data '
      || ' UNION ' ||
      'SELECT count(*) FROM '
      || quote_ident(atopology) || '.node '
    LOOP
      IF rec.count > 0 THEN
    RAISE EXCEPTION 'SQL/MM Spatial exception - non-empty view';
      END IF;
    END LOOP;

    -- face check is separated as it will contain a single (world)
    -- face record
    FOR rec in EXECUTE
      'SELECT count(*) FROM '
      || quote_ident(atopology) || '.face '
    LOOP
      IF rec.count != 1 THEN
    RAISE EXCEPTION 'SQL/MM Spatial exception - non-empty face view';
      END IF;
    END LOOP;

  EXCEPTION
    WHEN INVALID_SCHEMA_NAME THEN
      RAISE EXCEPTION 'SQL/MM Spatial exception - invalid topology name';
    WHEN UNDEFINED_TABLE THEN
      RAISE EXCEPTION 'SQL/MM Spatial exception - non-existent view';

  END; -- }


  --
  -- Node input linework with itself
  --
  WITH components AS ( SELECT geom FROM ST_Dump(acollection) )
  SELECT ST_UnaryUnion(ST_Collect(geom)) FROM (
    SELECT geom FROM components
      WHERE ST_Dimension(geom) = 1
    UNION ALL
    SELECT ST_Boundary(geom) FROM components
      WHERE ST_Dimension(geom) = 2
  ) as linework INTO STRICT nodededges;


  --
  -- Linemerge the resulting edges, to reduce the working set
  -- NOTE: this is more of a workaround for GEOS splitting overlapping
  --       lines to each of the segments.
  --
  SELECT ST_LineMerge(nodededges) INTO STRICT nodededges;


  --
  -- Collect input points and input lines endpoints
  --
  WITH components AS ( SELECT geom FROM ST_Dump(acollection) )
  SELECT ST_Union(geom) FROM (
    SELECT geom FROM components
      WHERE ST_Dimension(geom) = 0
    UNION ALL
    SELECT ST_Boundary(geom) FROM components
      WHERE ST_Dimension(geom) = 1
  ) as nodes INTO STRICT points;


  --
  -- Further split edges by points
  -- TODO: optimize this adding ST_Split support for multiline/multipoint
  --
  FOR rec IN SELECT geom FROM ST_Dump(points)
  LOOP
    -- Use the node to split edges
    SELECT ST_Collect(geom)
    FROM ST_Dump(ST_Split(nodededges, rec.geom))
    INTO STRICT nodededges;
  END LOOP;
  SELECT ST_UnaryUnion(nodededges) INTO STRICT nodededges;


  --
  -- Collect all nodes (from points and noded linework endpoints)
  --

  WITH edges AS ( SELECT geom FROM ST_Dump(nodededges) )
  SELECT ST_Union( -- TODO: ST_UnaryUnion ?
          COALESCE(ST_UnaryUnion(ST_Collect(geom)),
            ST_SetSRID('POINT EMPTY'::geometry, topoinfo.SRID)),
          COALESCE(points,
            ST_SetSRID('POINT EMPTY'::geometry, topoinfo.SRID))
         )
  FROM (
    SELECT ST_StartPoint(geom) as geom FROM edges
      UNION ALL
    SELECT ST_EndPoint(geom) FROM edges
  ) as endpoints INTO points;


  --
  -- Add all nodes as isolated so that
  -- later calls to AddEdgeModFace will tweak their being
  -- isolated or not...
  --
  FOR rec IN SELECT geom FROM ST_Dump(points)
  LOOP
    PERFORM topology.ST_AddIsoNode(atopology, 0, rec.geom);
  END LOOP;

  FOR rec IN SELECT geom FROM ST_Dump(nodededges)
  LOOP
    SELECT topology.GetNodeByPoint(atopology, st_startpoint(rec.geom), 0)
      INTO STRICT snode_id;
    SELECT topology.GetNodeByPoint(atopology, st_endpoint(rec.geom), 0)
      INTO STRICT enode_id;
    PERFORM topology.ST_AddEdgeModFace(atopology, snode_id, enode_id, rec.geom);
  END LOOP;

  RETURN 'Topology ' || atopology || ' populated';

END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_geometrytype(tg topology.topogeometry)
 RETURNS text
 LANGUAGE sql
 STABLE STRICT
AS $function$
	SELECT CASE
		WHEN type($1) = 1 THEN 'ST_MultiPoint'
		WHEN type($1) = 2 THEN 'ST_MultiLinestring'
		WHEN type($1) = 3 THEN 'ST_MultiPolygon'
		WHEN type($1) = 4 THEN 'ST_GeometryCollection'
		ELSE 'ST_Unexpected'
		END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_getfaceedges(toponame character varying, face_id integer)
 RETURNS SETOF topology.getfaceedges_returntype
 LANGUAGE c
 STABLE
AS '$libdir/postgis_topology-2.5', $function$ST_GetFaceEdges$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_getfacegeometry(toponame character varying, aface integer)
 RETURNS geometry
 LANGUAGE c
 STABLE
AS '$libdir/postgis_topology-2.5', $function$ST_GetFaceGeometry$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_inittopogeo(atopology character varying)
 RETURNS text
 LANGUAGE plpgsql
AS $function$
DECLARE
  rec RECORD;
  topology_id numeric;
BEGIN
  IF atopology IS NULL THEN
    RAISE EXCEPTION 'SQL/MM Spatial exception - null argument';
  END IF;

  FOR rec IN SELECT * FROM pg_namespace WHERE text(nspname) = atopology
  LOOP
    RAISE EXCEPTION 'SQL/MM Spatial exception - schema already exists';
  END LOOP;

  FOR rec IN EXECUTE 'SELECT topology.CreateTopology('
    ||quote_literal(atopology)|| ') as id'
  LOOP
    topology_id := rec.id;
  END LOOP;

  RETURN 'Topology-Geometry ' || quote_literal(atopology)
    || ' (id:' || topology_id || ') created.';
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_modedgeheal(toponame character varying, e1id integer, e2id integer)
 RETURNS integer
 LANGUAGE c
AS '$libdir/postgis_topology-2.5', $function$ST_ModEdgeHeal$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_modedgesplit(atopology character varying, anedge integer, apoint geometry)
 RETURNS integer
 LANGUAGE c
AS '$libdir/postgis_topology-2.5', $function$ST_ModEdgeSplit$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_moveisonode(atopology character varying, anode integer, apoint geometry)
 RETURNS text
 LANGUAGE c
AS '$libdir/postgis_topology-2.5', $function$ST_MoveIsoNode$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_newedgeheal(toponame character varying, e1id integer, e2id integer)
 RETURNS integer
 LANGUAGE c
AS '$libdir/postgis_topology-2.5', $function$ST_NewEdgeHeal$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_newedgessplit(atopology character varying, anedge integer, apoint geometry)
 RETURNS integer
 LANGUAGE c
AS '$libdir/postgis_topology-2.5', $function$ST_NewEdgesSplit$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_remedgemodface(toponame character varying, e1id integer)
 RETURNS integer
 LANGUAGE c
AS '$libdir/postgis_topology-2.5', $function$ST_RemEdgeModFace$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_remedgenewface(toponame character varying, e1id integer)
 RETURNS integer
 LANGUAGE c
AS '$libdir/postgis_topology-2.5', $function$ST_RemEdgeNewFace$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_remisonode(character varying, integer)
 RETURNS text
 LANGUAGE c
AS '$libdir/postgis_topology-2.5', $function$ST_RemoveIsoNode$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_removeisoedge(atopology character varying, anedge integer)
 RETURNS text
 LANGUAGE c
AS '$libdir/postgis_topology-2.5', $function$ST_RemIsoEdge$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_removeisonode(atopology character varying, anode integer)
 RETURNS text
 LANGUAGE c
AS '$libdir/postgis_topology-2.5', $function$ST_RemoveIsoNode$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.st_simplify(tg topology.topogeometry, tolerance double precision)
 RETURNS geometry
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
  topology_info RECORD;
  layer_info RECORD;
  child_layer_info RECORD;
  geom geometry;
  sql TEXT;
BEGIN

  -- Get topology information
  SELECT id, name FROM topology.topology
    INTO topology_info
    WHERE id = tg.topology_id;
  IF NOT FOUND THEN
      RAISE EXCEPTION 'No topology with id "%" in topology.topology', tg.topology_id;
  END IF;

  -- Get layer info
  SELECT * FROM topology.layer
    WHERE topology_id = tg.topology_id
    AND layer_id = tg.layer_id
    INTO layer_info;
  IF NOT FOUND THEN
    RAISE EXCEPTION 'Could not find TopoGeometry layer % in topology %', tg.layer_id, tg.topology_id;
  END IF;

  --
  -- If this feature layer is on any level > 0 we will
  -- compute the topological union of all simplified child
  -- features in fact recursing.
  --
  IF layer_info.level > 0 THEN -- {

    -- Get child layer info
    SELECT * FROM topology.layer WHERE layer_id = layer_info.child_id
      AND topology_id = tg.topology_id
      INTO child_layer_info;
    IF NOT FOUND THEN
      RAISE EXCEPTION 'Invalid layer % in topology % (unexistent child layer %)', tg.layer_id, tg.topology_id, layer_info.child_id;
    END IF;

    sql := 'SELECT st_multi(st_union(topology.ST_Simplify('
      || quote_ident(child_layer_info.feature_column)
      || ',' || tolerance || '))) as geom FROM '
      || quote_ident(child_layer_info.schema_name) || '.'
      || quote_ident(child_layer_info.table_name)
      || ', ' || quote_ident(topology_info.name) || '.relation pr'
      || ' WHERE '
      || ' pr.topogeo_id = ' || tg.id
      || ' AND '
      || ' pr.layer_id = ' || tg.layer_id
      || ' AND '
      || ' id('||quote_ident(child_layer_info.feature_column)
      || ') = pr.element_id '
      || ' AND '
      || 'layer_id('||quote_ident(child_layer_info.feature_column)
      || ') = pr.element_type ';
    RAISE DEBUG '%', sql;
    EXECUTE sql INTO geom;

  ELSIF tg.type = 3 THEN -- [multi]polygon -- }{

    -- TODO: use ST_GetFaceEdges
    -- TODO: is st_unaryunion needed?
    sql := 'SELECT st_multi(st_unaryunion(ST_BuildArea(ST_Node(ST_Collect(ST_Simplify(geom, '
      || tolerance || ')))))) as geom FROM '
      || quote_ident(topology_info.name)
      || '.edge_data e, '
      || quote_ident(topology_info.name)
      || '.relation r WHERE ( e.left_face = r.element_id'
      || ' OR e.right_face = r.element_id )'
      || ' AND r.topogeo_id = ' || tg.id
      || ' AND r.layer_id = ' || tg.layer_id
      || ' AND element_type = 3 ';
    RAISE DEBUG '%', sql;
    EXECUTE sql INTO geom;

  ELSIF tg.type = 2 THEN -- [multi]line -- }{

    sql :=
      'SELECT st_multi(ST_LineMerge(ST_Node(ST_Collect(ST_Simplify(e.geom,'
      || tolerance || '))))) as g FROM '
      || quote_ident(topology_info.name) || '.edge e, '
      || quote_ident(topology_info.name) || '.relation r '
      || ' WHERE r.topogeo_id = ' || tg.id
      || ' AND r.layer_id = ' || tg.layer_id
      || ' AND r.element_type = 2 '
      || ' AND abs(r.element_id) = e.edge_id';
    EXECUTE sql INTO geom;

  ELSIF tg.type = 1 THEN -- [multi]point -- }{

    -- Can't simplify points...
    geom := topology.Geometry(tg);

  ELSIF tg.type = 4 THEN -- mixed collection -- }{

   sql := 'WITH areas AS ( '
      || 'SELECT st_multi(st_union(ST_BuildArea(ST_Node(ST_Collect(ST_Simplify(geom, '
      || tolerance || ')))) as geom FROM '
      || quote_ident(topology_info.name)
      || '.edge_data e, '
      || quote_ident(topology_info.name)
      || '.relation r WHERE ( e.left_face = r.element_id'
      || ' OR e.right_face = r.element_id )'
      || ' AND r.topogeo_id = ' || tg.id
      || ' AND r.layer_id = ' || tg.layer_id
      || ' AND element_type = 3 ), '
      || 'lines AS ( '
      || 'SELECT st_multi(ST_LineMerge(ST_Collect(ST_Simplify(e.geom,'
      || tolerance || ')))) as g FROM '
      || quote_ident(topology_info.name) || '.edge e, '
      || quote_ident(topology_info.name) || '.relation r '
      || ' WHERE r.topogeo_id = ' || tg.id
      || ' AND r.layer_id = ' || tg.layer_id
      || ' AND r.element_type = 2 '
      || ' AND abs(r.element_id) = e.edge_id ), '
      || ' points as ( SELECT st_union(n.geom) as g FROM '
      || quote_ident(topology_info.name) || '.node n, '
      || quote_ident(topology_info.name) || '.relation r '
      || ' WHERE r.topogeo_id = ' || tg.id
      || ' AND r.layer_id = ' || tg.layer_id
      || ' AND r.element_type = 1 '
      || ' AND r.element_id = n.node_id ), '
      || ' un as ( SELECT g FROM areas UNION ALL SELECT g FROM lines '
      || '          UNION ALL SELECT g FROM points ) '
      || 'SELECT ST_Multi(ST_Collect(g)) FROM un';
    EXECUTE sql INTO geom;

  ELSE -- }{

    RAISE EXCEPTION 'Invalid TopoGeometries (unknown type %)', tg.type;

  END IF; -- }

  RETURN geom;

END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.topoelementarray_append(topology.topoelementarray, topology.topoelement)
 RETURNS topology.topoelementarray
 LANGUAGE sql
 IMMUTABLE
AS $function$
	SELECT CASE
		WHEN $1 IS NULL THEN
			topology.TopoElementArray('{' || $2::text || '}')
		ELSE
			topology.TopoElementArray($1::int[][]||$2::int[])
		END;
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.topogeo_addgeometry(atopology character varying, ageom geometry, tolerance double precision DEFAULT 0)
 RETURNS void
 LANGUAGE plpgsql
AS $function$
DECLARE
BEGIN
	RAISE EXCEPTION 'TopoGeo_AddGeometry not implemented yet';
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.topogeo_addlinestring(atopology character varying, aline geometry, tolerance double precision DEFAULT 0)
 RETURNS SETOF integer
 LANGUAGE c
AS '$libdir/postgis_topology-2.5', $function$TopoGeo_AddLinestring$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.topogeo_addpoint(atopology character varying, apoint geometry, tolerance double precision DEFAULT 0)
 RETURNS integer
 LANGUAGE c
AS '$libdir/postgis_topology-2.5', $function$TopoGeo_AddPoint$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.topogeo_addpolygon(atopology character varying, apoly geometry, tolerance double precision DEFAULT 0)
 RETURNS SETOF integer
 LANGUAGE c
AS '$libdir/postgis_topology-2.5', $function$TopoGeo_AddPolygon$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.topogeom_addelement(tg topology.topogeometry, el topology.topoelement)
 RETURNS topology.topogeometry
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
  toponame TEXT;
  sql TEXT;
BEGIN

  -- Get topology name
  BEGIN
    SELECT name
    FROM topology.topology
      INTO STRICT toponame WHERE id = topology_id(tg);
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      RAISE EXCEPTION 'No topology with name "%" in topology.topology',
        atopology;
  END;

  -- Insert new element
  sql := format('INSERT INTO %s.relation'
         '(topogeo_id,layer_id,element_id,element_type)'
         ' VALUES($1,$2,$3,$4)', quote_ident(toponame));
  BEGIN
    EXECUTE sql USING id(tg), layer_id(tg), el[1], el[2];
  EXCEPTION
    WHEN unique_violation THEN
      -- already present, let go
    WHEN OTHERS THEN
      RAISE EXCEPTION 'Got % (%)', SQLERRM, SQLSTATE;
  END;

  RETURN tg;

END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.topogeom_remelement(tg topology.topogeometry, el topology.topoelement)
 RETURNS topology.topogeometry
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
  toponame TEXT;
  sql TEXT;
BEGIN

  -- Get topology name
  BEGIN
    SELECT name
    FROM topology.topology
      INTO STRICT toponame WHERE id = topology_id(tg);
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      RAISE EXCEPTION 'No topology with name "%" in topology.topology',
        atopology;
  END;

  -- Delete the element
  sql := format('DELETE FROM %s.relation WHERE '
         'topogeo_id = $1 AND layer_id = $2 AND '
         'element_id = $3 AND element_type = $4',
         quote_ident(toponame));
  EXECUTE sql USING id(tg), layer_id(tg), el[1], el[2];

  RETURN tg;

END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.topologysummary(atopology character varying)
 RETURNS text
 LANGUAGE plpgsql
 STABLE STRICT
AS $function$
DECLARE
  rec RECORD;
  rec2 RECORD;
  var_topology_id integer;
  n int4;
  missing int4;
  sql text;
  ret text;
  tgcount int4;
BEGIN

  ret := 'Topology ' || quote_ident(atopology) ;

  BEGIN
    SELECT * FROM topology.topology WHERE name = atopology INTO STRICT rec;
    -- TODO: catch <no_rows> to give a nice error message
    var_topology_id := rec.id;

    ret := ret || ' (id ' || rec.id || ', '
               || 'SRID ' || rec.srid || ', '
               || 'precision ' || rec.precision;
    IF rec.hasz THEN ret := ret || ', has Z'; END IF;
    ret := ret || E')\n';
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      ret := ret || E' (unregistered)\n';
  END;

  BEGIN
    EXECUTE 'SELECT count(*) FROM ' || quote_ident(atopology)
      || '.node ' INTO STRICT n;
    ret = ret || n || ' nodes, ';
  EXCEPTION
    WHEN UNDEFINED_TABLE OR INVALID_SCHEMA_NAME THEN
      IF NOT EXISTS (
          SELECT * FROM pg_catalog.pg_namespace WHERE nspname = atopology
         )
      THEN
        ret = ret || 'missing schema';
        RETURN ret;
      ELSE
        ret = ret || 'missing nodes, ';
      END IF;
  END;

  BEGIN
    EXECUTE 'SELECT count(*) FROM ' || quote_ident(atopology)
      || '.edge' INTO STRICT n;
    ret = ret || n || ' edges, ';
  EXCEPTION
    WHEN UNDEFINED_TABLE OR INVALID_SCHEMA_NAME THEN
      ret = ret || 'missing edges, ';
  END;

  BEGIN
    EXECUTE 'SELECT count(*) FROM ' || quote_ident(atopology)
      || '.face' INTO STRICT n;
    ret = ret || greatest(n-1,0) || ' faces, '; -- -1 is face=0
  EXCEPTION
    WHEN UNDEFINED_TABLE OR INVALID_SCHEMA_NAME THEN
      ret = ret || 'missing faces, ';
  END;

  BEGIN
    EXECUTE 'SELECT count(distinct layer_id) AS ln, '
      || 'count(distinct (layer_id,topogeo_id)) AS tn FROM '
      || quote_ident(atopology) || '.relation' INTO STRICT rec;
    tgcount := rec.tn;
    ret = ret || rec.tn || ' topogeoms in ' || rec.ln || E' layers\n';
  EXCEPTION
    WHEN UNDEFINED_TABLE THEN
      ret = ret || E'missing relations\n';
    WHEN UNDEFINED_COLUMN THEN
      ret = ret || E'corrupted relations\n';
  END;

  -- print information about registered layers
  FOR rec IN SELECT * FROM topology.layer l
    WHERE l.topology_id = var_topology_id
    ORDER by layer_id
  LOOP -- {
    ret = ret || 'Layer ' || rec.layer_id || ', type ';
    CASE
      WHEN rec.feature_type = 1 THEN
        ret = ret || 'Puntal';
      WHEN rec.feature_type = 2 THEN
        ret = ret || 'Lineal';
      WHEN rec.feature_type = 3 THEN
        ret = ret || 'Polygonal';
      WHEN rec.feature_type = 4 THEN
        ret = ret || 'Mixed';
      ELSE
        ret = ret || '???';
    END CASE;

    ret = ret || ' (' || rec.feature_type || '), ';

    BEGIN

      EXECUTE 'SELECT count(*) FROM ( SELECT DISTINCT topogeo_id FROM '
        || quote_ident(atopology)
        || '.relation r WHERE r.layer_id = ' || rec.layer_id
        || ' ) foo ' INTO STRICT n;

      ret = ret || n || ' topogeoms' || E'\n';

    EXCEPTION WHEN UNDEFINED_TABLE OR UNDEFINED_COLUMN THEN
      n := NULL;
      ret = ret || 'X topogeoms' || E'\n';
    END;

      IF rec.level > 0 THEN
        ret = ret || ' Hierarchy level ' || rec.level
                  || ', child layer ' || rec.child_id || E'\n';
      END IF;

      ret = ret || ' Deploy: ';
      IF rec.feature_column != '' THEN
        ret = ret || quote_ident(rec.schema_name) || '.'
                  || quote_ident(rec.table_name) || '.'
                  || quote_ident(rec.feature_column);

        IF n > 0 THEN
          sql := 'SELECT count(*) FROM ( SELECT topogeo_id FROM '
            || quote_ident(atopology)
            || '.relation r WHERE r.layer_id = ' || rec.layer_id
            || ' EXCEPT SELECT DISTINCT id('
            || quote_ident(rec.feature_column) || ') FROM '
            || quote_ident(rec.schema_name) || '.'
            || quote_ident(rec.table_name) || ') as foo';
          BEGIN
            EXECUTE sql INTO STRICT missing;
            IF missing > 0 THEN
              ret = ret || ' (' || missing || ' missing topogeoms)';
            END IF;
          EXCEPTION
            WHEN UNDEFINED_TABLE THEN
              ret = ret || ' ( unexistent table )';
            WHEN UNDEFINED_COLUMN THEN
              ret = ret || ' ( unexistent column )';
          END;
        END IF;
        ret = ret || E'\n';

      ELSE
        ret = ret || E'NONE (detached)\n';
      END IF;

  END LOOP; -- }

  -- print information about unregistered layers containing topogeoms
  IF tgcount > 0 THEN -- {

    sql := 'SELECT layer_id FROM '
        || quote_ident(atopology) || '.relation EXCEPT SELECT layer_id'
        || ' FROM topology.layer WHERE topology_id = $1 ORDER BY layer_id';
    --RAISE DEBUG '%', sql;
    FOR rec IN  EXECUTE sql USING var_topology_id
    LOOP -- {
      ret = ret || 'Layer ' || rec.layer_id::text || ', UNREGISTERED, ';

      EXECUTE 'SELECT count(*) FROM ( SELECT DISTINCT topogeo_id FROM '
        || quote_ident(atopology)
        || '.relation r WHERE r.layer_id = ' || rec.layer_id
        || ' ) foo ' INTO STRICT n;

      ret = ret || n || ' topogeoms' || E'\n';

    END LOOP; -- }

  END IF; -- }

  RETURN ret;
END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.totopogeom(ageom geometry, atopology character varying, alayer integer, atolerance double precision DEFAULT 0)
 RETURNS topology.topogeometry
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
  layer_info RECORD;
  topology_info RECORD;
  tg topology.TopoGeometry;
  typ TEXT;
BEGIN

  -- Get topology information
  BEGIN
    SELECT *
    FROM topology.topology
      INTO STRICT topology_info WHERE name = atopology;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      RAISE EXCEPTION 'No topology with name "%" in topology.topology',
        atopology;
  END;

  -- Get layer information
  BEGIN
    SELECT *, CASE
      WHEN feature_type = 1 THEN 'puntal'
      WHEN feature_type = 2 THEN 'lineal'
      WHEN feature_type = 3 THEN 'areal'
      WHEN feature_type = 4 THEN 'mixed'
      ELSE 'unexpected_'||feature_type
      END as typename
    FROM topology.layer l
      INTO STRICT layer_info
      WHERE l.layer_id = alayer
      AND l.topology_id = topology_info.id;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      RAISE EXCEPTION 'No layer with id "%" in topology "%"',
        alayer, atopology;
  END;

  -- Can't convert to a hierarchical topogeometry
  IF layer_info.level > 0 THEN
      RAISE EXCEPTION 'Layer "%" of topology "%" is hierarchical, cannot convert to it.',
        alayer, atopology;
  END IF;

  --
  -- Check type compatibility and create empty TopoGeometry
  -- 1:puntal, 2:lineal, 3:areal, 4:collection
  --
  typ = geometrytype(ageom);
  IF typ = 'GEOMETRYCOLLECTION' THEN
    --  A collection can only go collection layer
    IF layer_info.feature_type != 4 THEN
      RAISE EXCEPTION
        'Layer "%" of topology "%" is %, cannot hold a collection feature.',
        layer_info.layer_id, topology_info.name, layer_info.typename;
    END IF;
    tg := topology.CreateTopoGeom(atopology, 4, alayer);
  ELSIF typ = 'POINT' OR typ = 'MULTIPOINT' THEN -- puntal
    --  A point can go in puntal or collection layer
    IF layer_info.feature_type != 4 and layer_info.feature_type != 1 THEN
      RAISE EXCEPTION
        'Layer "%" of topology "%" is %, cannot hold a puntal feature.',
        layer_info.layer_id, topology_info.name, layer_info.typename;
    END IF;
    tg := topology.CreateTopoGeom(atopology, 1, alayer);
  ELSIF typ = 'LINESTRING' or typ = 'MULTILINESTRING' THEN -- lineal
    --  A line can go in lineal or collection layer
    IF layer_info.feature_type != 4 and layer_info.feature_type != 2 THEN
      RAISE EXCEPTION
        'Layer "%" of topology "%" is %, cannot hold a lineal feature.',
        layer_info.layer_id, topology_info.name, layer_info.typename;
    END IF;
    tg := topology.CreateTopoGeom(atopology, 2, alayer);
  ELSIF typ = 'POLYGON' OR typ = 'MULTIPOLYGON' THEN -- areal
    --  An area can go in areal or collection layer
    IF layer_info.feature_type != 4 and layer_info.feature_type != 3 THEN
      RAISE EXCEPTION
        'Layer "%" of topology "%" is %, cannot hold an areal feature.',
        layer_info.layer_id, topology_info.name, layer_info.typename;
    END IF;
    tg := topology.CreateTopoGeom(atopology, 3, alayer);
  ELSE
      -- Should never happen
      RAISE EXCEPTION
        'Unsupported feature type %', typ;
  END IF;

  tg := topology.toTopoGeom(ageom, tg, atolerance);

  RETURN tg;

END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.totopogeom(ageom geometry, tg topology.topogeometry, atolerance double precision DEFAULT 0)
 RETURNS topology.topogeometry
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
  layer_info RECORD;
  topology_info RECORD;
  rec RECORD;
  rec2 RECORD;
  elem TEXT;
  elems TEXT[];
  sql TEXT;
  typ TEXT;
  tolerance FLOAT8;
  alayer INT;
  atopology TEXT;
BEGIN


  -- Get topology information
  SELECT id, name FROM topology.topology
    INTO topology_info
    WHERE id = topology_id(tg);
  IF NOT FOUND THEN
    RAISE EXCEPTION 'No topology with id "%" in topology.topology',
                    topology_id(tg);
  END IF;

  alayer := layer_id(tg);
  atopology := topology_info.name;

  -- Get tolerance, if 0 was given
  tolerance := COALESCE( NULLIF(atolerance, 0), topology._st_mintolerance(topology_info.name, ageom) );

  -- Get layer information
  BEGIN
    SELECT *, CASE
      WHEN feature_type = 1 THEN 'puntal'
      WHEN feature_type = 2 THEN 'lineal'
      WHEN feature_type = 3 THEN 'areal'
      WHEN feature_type = 4 THEN 'mixed'
      ELSE 'unexpected_'||feature_type
      END as typename
    FROM topology.layer l
      INTO STRICT layer_info
      WHERE l.layer_id = layer_id(tg)
      AND l.topology_id = topology_info.id;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      RAISE EXCEPTION 'No layer with id "%" in topology "%"',
        alayer, atopology;
  END;

  -- Can't convert to a hierarchical topogeometry
  IF layer_info.level > 0 THEN
      RAISE EXCEPTION 'Layer "%" of topology "%" is hierarchical, cannot convert a simple geometry to it.',
        alayer, atopology;
  END IF;

  --
  -- Check type compatibility and set TopoGeometry type
  -- 1:puntal, 2:lineal, 3:areal, 4:collection
  --
  typ = geometrytype(ageom);
  IF typ = 'GEOMETRYCOLLECTION' THEN
    --  A collection can only go to collection layer
    IF layer_info.feature_type != 4 THEN
      RAISE EXCEPTION
        'Layer "%" of topology "%" is %, cannot hold a collection feature.',
        layer_info.layer_id, topology_info.name, layer_info.typename;
    END IF;
    tg.type := 4;
  ELSIF typ = 'POINT' OR typ = 'MULTIPOINT' THEN -- puntal
    --  A point can go in puntal or collection layer
    IF layer_info.feature_type != 4 and layer_info.feature_type != 1 THEN
      RAISE EXCEPTION
        'Layer "%" of topology "%" is %, cannot hold a puntal feature.',
        layer_info.layer_id, topology_info.name, layer_info.typename;
    END IF;
    tg.type := 1;
  ELSIF typ = 'LINESTRING' or typ = 'MULTILINESTRING' THEN -- lineal
    --  A line can go in lineal or collection layer
    IF layer_info.feature_type != 4 and layer_info.feature_type != 2 THEN
      RAISE EXCEPTION
        'Layer "%" of topology "%" is %, cannot hold a lineal feature.',
        layer_info.layer_id, topology_info.name, layer_info.typename;
    END IF;
    tg.type := 2;
  ELSIF typ = 'POLYGON' OR typ = 'MULTIPOLYGON' THEN -- areal
    --  An area can go in areal or collection layer
    IF layer_info.feature_type != 4 and layer_info.feature_type != 3 THEN
      RAISE EXCEPTION
        'Layer "%" of topology "%" is %, cannot hold an areal feature.',
        layer_info.layer_id, topology_info.name, layer_info.typename;
    END IF;
    tg.type := 3;
  ELSE
      -- Should never happen
      RAISE EXCEPTION
        'Unexpected feature dimension %', ST_Dimension(ageom);
  END IF;

  -- Now that we have an empty topogeometry, we loop over distinct components
  -- and add them to the definition of it. We add them as soon
  -- as possible so that each element can further edit the
  -- definition by splitting
  FOR rec IN SELECT id(tg), alayer as lyr,
    geom, ST_Dimension(gd.geom) as dims
    FROM ST_Dump(ageom) AS gd
    WHERE NOT ST_IsEmpty(gd.geom)
  LOOP
    -- NOTE: Switched from using case to this because of PG 10 behavior change
    -- Using a UNION ALL only one will be processed because of the WHERE
    -- Since the WHERE clause will be processed first
    FOR rec2 IN SELECT primitive
          FROM
            (
              SELECT topology.topogeo_addPoint(atopology, rec.geom, tolerance)
                WHERE rec.dims = 0
              UNION ALL
              SELECT topology.topogeo_addLineString(atopology, rec.geom, tolerance)
                WHERE rec.dims = 1
              UNION ALL
              SELECT topology.topogeo_addPolygon(atopology, rec.geom, tolerance)
                WHERE rec.dims = 2
            ) AS f(primitive)
    LOOP
      elem := ARRAY[rec.dims+1, rec2.primitive]::text;
      IF elems @> ARRAY[elem] THEN
      ELSE
        elems := elems || elem;
        -- TODO: consider use a single INSERT statement for the whole thing
        sql := 'INSERT INTO ' || quote_ident(atopology)
            || '.relation(topogeo_id, layer_id, element_type, element_id) VALUES ('
            || rec.id || ',' || rec.lyr || ',' || rec.dims+1
            || ',' || rec2.primitive || ')'
            -- NOTE: we're avoiding duplicated rows here
            || ' EXCEPT SELECT ' || rec.id || ', ' || rec.lyr
            || ', element_type, element_id FROM '
            || quote_ident(topology_info.name)
            || '.relation WHERE layer_id = ' || rec.lyr
            || ' AND topogeo_id = ' || rec.id;
        EXECUTE sql;
      END IF;
    END LOOP;
  END LOOP;

  RETURN tg;

END
$function$
 ;
-- STATEMENT-END
-- Add
-- STATEMENT-BEGIN
CREATE OR REPLACE FUNCTION topology.validatetopology(toponame character varying)
 RETURNS SETOF topology.validatetopology_returntype
 LANGUAGE plpgsql
 STRICT
AS $function$
DECLARE
  retrec topology.ValidateTopology_ReturnType;
  rec RECORD;
  rec2 RECORD;
  i integer;
  invalid_edges integer[];
  invalid_faces integer[];
  sql text;
BEGIN

  -- Check for coincident nodes
  FOR rec IN EXECUTE 'SELECT a.node_id as id1, b.node_id as id2 FROM '
    || quote_ident(toponame) || '.node a, '
    || quote_ident(toponame) || '.node b '
       'WHERE a.node_id < b.node_id '
       ' AND ST_DWithin(a.geom, b.geom, 0)' -- NOTE: see #1625 and #1789
  LOOP
    retrec.error = 'coincident nodes';
    retrec.id1 = rec.id1;
    retrec.id2 = rec.id2;
    RETURN NEXT retrec;
  END LOOP;

  -- Check for edge crossed nodes
  -- TODO: do this in the single edge loop
  FOR rec IN EXECUTE 'SELECT n.node_id as nid, e.edge_id as eid FROM '
    || quote_ident(toponame) || '.node n, '
    || quote_ident(toponame) || '.edge e '
       'WHERE e.start_node != n.node_id '
       'AND e.end_node != n.node_id '
       'AND ST_Within(n.geom, e.geom)'
  LOOP
    retrec.error = 'edge crosses node';
    retrec.id1 = rec.eid; -- edge_id
    retrec.id2 = rec.nid; -- node_id
    RETURN NEXT retrec;
  END LOOP;

  -- Scan all edges
  FOR rec IN EXECUTE 'SELECT e.geom, e.edge_id as id1, e.left_face, e.right_face FROM '
    || quote_ident(toponame) || '.edge e ORDER BY edge_id'
  LOOP

    -- Any invalid edge becomes a cancer for higher level complexes
    IF NOT ST_IsValid(rec.geom) THEN

      retrec.error = 'invalid edge';
      retrec.id1 = rec.id1;
      retrec.id2 = NULL;
      RETURN NEXT retrec;
      invalid_edges := array_append(invalid_edges, rec.id1);

      IF invalid_faces IS NULL OR NOT rec.left_face = ANY ( invalid_faces )
      THEN
        invalid_faces := array_append(invalid_faces, rec.left_face);
      END IF;

      IF rec.right_face != rec.left_face AND ( invalid_faces IS NULL OR
            NOT rec.right_face = ANY ( invalid_faces ) )
      THEN
        invalid_faces := array_append(invalid_faces, rec.right_face);
      END IF;

      CONTINUE;

    END IF;

    IF NOT ST_IsSimple(rec.geom) THEN
      retrec.error = 'edge not simple';
      retrec.id1 = rec.id1;
      retrec.id2 = NULL;
      RETURN NEXT retrec;
    END IF;

  END LOOP;

  -- Check for edge crossing
  sql := 'SELECT e1.edge_id as id1, e2.edge_id as id2, '
       ' e1.geom as g1, e2.geom as g2, '
       'ST_Relate(e1.geom, e2.geom) as im FROM '
    || quote_ident(toponame) || '.edge e1, '
    || quote_ident(toponame) || '.edge e2 '
       'WHERE e1.edge_id < e2.edge_id '
       ' AND e1.geom && e2.geom ';
  IF invalid_edges IS NOT NULL THEN
    sql := sql || ' AND NOT e1.edge_id = ANY ('
               || quote_literal(invalid_edges) || ')'
               || ' AND NOT e2.edge_id = ANY ('
               || quote_literal(invalid_edges) || ')';
  END IF;

  FOR rec IN EXECUTE sql
  LOOP

    IF ST_RelateMatch(rec.im, 'FF1F**1*2') THEN
      CONTINUE; -- no interior intersection

    --
    -- Closed lines have no boundary, so endpoint
    -- intersection would be considered interior
    -- See http://trac.osgeo.org/postgis/ticket/770
    -- See also full explanation in topology.AddEdge
    --

    ELSIF ST_RelateMatch(rec.im, 'FF10F01F2') THEN
      -- first line (g1) is open, second (g2) is closed
      -- first boundary has puntual intersection with second interior
      --
      -- compute intersection, check it equals second endpoint
      IF ST_Equals(ST_Intersection(rec.g2, rec.g1),
                   ST_StartPoint(rec.g2))
      THEN
        CONTINUE;
      END IF;

    ELSIF ST_RelateMatch(rec.im, 'F01FFF102') THEN
      -- second line (g2) is open, first (g1) is closed
      -- second boundary has puntual intersection with first interior
      --
      -- compute intersection, check it equals first endpoint
      IF ST_Equals(ST_Intersection(rec.g2, rec.g1),
                   ST_StartPoint(rec.g1))
      THEN
        CONTINUE;
      END IF;

    ELSIF ST_RelateMatch(rec.im, '0F1FFF1F2') THEN
      -- both lines are closed (boundary intersects nothing)
      -- they have puntual intersection between interiors
      --
      -- compute intersection, check it's a single point
      -- and equals first StartPoint _and_ second StartPoint
      IF ST_Equals(ST_Intersection(rec.g1, rec.g2),
                   ST_StartPoint(rec.g1)) AND
         ST_Equals(ST_StartPoint(rec.g1), ST_StartPoint(rec.g2))
      THEN
        CONTINUE;
      END IF;

    END IF;

    retrec.error = 'edge crosses edge';
    retrec.id1 = rec.id1;
    retrec.id2 = rec.id2;
    RETURN NEXT retrec;
  END LOOP;

  -- Check for edge start_node geometry mis-match
  -- TODO: move this in the first edge table scan
  FOR rec IN EXECUTE 'SELECT e.edge_id as id1, n.node_id as id2 FROM '
    || quote_ident(toponame) || '.edge e, '
    || quote_ident(toponame) || '.node n '
       'WHERE e.start_node = n.node_id '
       'AND NOT ST_Equals(ST_StartPoint(e.geom), n.geom)'
  LOOP
    retrec.error = 'edge start node geometry mis-match';
    retrec.id1 = rec.id1;
    retrec.id2 = rec.id2;
    RETURN NEXT retrec;
  END LOOP;

  -- Check for edge end_node geometry mis-match
  -- TODO: move this in the first edge table scan
  FOR rec IN EXECUTE 'SELECT e.edge_id as id1, n.node_id as id2 FROM '
    || quote_ident(toponame) || '.edge e, '
    || quote_ident(toponame) || '.node n '
       'WHERE e.end_node = n.node_id '
       'AND NOT ST_Equals(ST_EndPoint(e.geom), n.geom)'
  LOOP
    retrec.error = 'edge end node geometry mis-match';
    retrec.id1 = rec.id1;
    retrec.id2 = rec.id2;
    RETURN NEXT retrec;
  END LOOP;

  -- Check for faces w/out edges
  FOR rec IN EXECUTE 'SELECT face_id as id1 FROM '
    || quote_ident(toponame) || '.face '
    || 'WHERE face_id > 0 EXCEPT ( SELECT left_face FROM '
    || quote_ident(toponame) || '.edge '
    || ' UNION SELECT right_face FROM '
    || quote_ident(toponame) || '.edge '
    || ')'
  LOOP
    retrec.error = 'face without edges';
    retrec.id1 = rec.id1;
    retrec.id2 = NULL;
    RETURN NEXT retrec;
  END LOOP;

  -- Now create a temporary table to construct all face geometries
  -- for checking their consistency

  sql := 'CREATE TEMP TABLE face_check ON COMMIT DROP AS '
       'SELECT face_id, topology.ST_GetFaceGeometry('
    || quote_literal(toponame) || ', face_id) as geom, mbr FROM '
    || quote_ident(toponame) || '.face WHERE face_id > 0';
  IF invalid_faces IS NOT NULL THEN
    sql := sql || ' AND NOT face_id = ANY ('
               || quote_literal(invalid_faces) || ')';
  END IF;
  EXECUTE sql;

  -- Build a gist index on geom
  EXECUTE 'CREATE INDEX "face_check_gist" ON '
       'face_check USING gist (geom);';

  -- Build a btree index on id
  EXECUTE 'CREATE INDEX "face_check_bt" ON '
       'face_check (face_id);';

  -- Scan the table looking for NULL geometries
  FOR rec IN EXECUTE
    'SELECT f1.face_id FROM '
       'face_check f1 WHERE f1.geom IS NULL OR ST_IsEmpty(f1.geom)'
  LOOP
    -- Face missing !
    retrec.error := 'face has no rings';
    retrec.id1 := rec.face_id;
    retrec.id2 := NULL;
    RETURN NEXT retrec;
  END LOOP;

  -- Scan the table looking for overlap or containment
  -- TODO: also check for MBR consistency
  FOR rec IN EXECUTE
    'SELECT f1.geom, f1.face_id as id1, f2.face_id as id2, '
       ' ST_Relate(f1.geom, f2.geom) as im'
       ' FROM '
       'face_check f1, '
       'face_check f2 '
       'WHERE f1.face_id < f2.face_id'
       ' AND f1.geom && f2.geom'
  LOOP

    -- Face overlap
    IF ST_RelateMatch(rec.im, 'T*T***T**') THEN
    retrec.error = 'face overlaps face';
    retrec.id1 = rec.id1;
    retrec.id2 = rec.id2;
    RETURN NEXT retrec;
    END IF;

    -- Face 1 is within face 2
    IF ST_RelateMatch(rec.im, 'T*F**F***') THEN
    retrec.error = 'face within face';
    retrec.id1 = rec.id1;
    retrec.id2 = rec.id2;
    RETURN NEXT retrec;
    END IF;

    -- Face 1 contains face 2
    IF ST_RelateMatch(rec.im, 'T*****FF*') THEN
    retrec.error = 'face within face';
    retrec.id1 = rec.id2;
    retrec.id2 = rec.id1;
    RETURN NEXT retrec;
    END IF;

  END LOOP;


  DROP TABLE face_check;

  RETURN;
END
$function$
 ;
-- STATEMENT-END
