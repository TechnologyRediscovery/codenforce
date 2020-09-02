INSERT INTO public.eventcategory(
         categoryid, categorytype,
         title,
         description,
         notifymonitors, hidable, icon_iconid, relativeorderwithintype, relativeorderglobal,
         hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins,
         active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
 VALUES (308, 'PropertyInfoCase'::eventtype,
 		 'ParcelNotInCountyPortal',
 		 'Documents when a parcel is in the CodeNForce database that was not found in the Allegheny County''s Real Estate Portal',
 		 TRUE, TRUE, NULL, 0, 0,
         NULL, NULL, 1,
         TRUE, 7, 3, 7);
