-- ****************************************************************************
-- Early versions of the database started some sequences below the columns maximum value.
  -- Thus, adding new entities would sometimes cause a collision.
  -- This function provides a way to reset the values during database patches,
  --   as hardcoding a new sequence starting value would cause a collision every time the database would be created from patches.
  -- Unfortunately, this breaks ALTER SEQUENCE '{seq_name}' RESTART; as the starting value is still the old starting value
  -- Fixes are very much appreciated.

  -- AUTHOR: SNAPPER VIBES
-- ****************************************************************************
select resetsequences();