INSERT INTO public.eventcategory(
	 categoryid, categorytype,
	 title,
	 description,
	 notifymonitors, hidable, icon_iconid, relativeorderwithintype, relativeorderglobal,
	 hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins,
	 active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
 VALUES
	(308, 'PropertyInfoCase'::eventtype,
		'DifferentMunicode',
		'Documents a change in a Parcel''s municode (compared to the Allegheny County Real Estate Portal)',
		TRUE, TRUE, NULL, 0, 0,
		NULL, NULL, 1,
		TRUE, 7, 3, 7),
	(309, 'PropertyInfoCase'::eventtype,
		'NotInRealEstatePortal',
		'Documents when a parcel is in the CodeNForce database but the Allegheny County Real Estate Portal''s corresponding page is blank',
		TRUE, TRUE, NULL, 0, 0,
		NULL, NULL, 1,
		TRUE, 7, 3, 7);
