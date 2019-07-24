

DROP TABLE moneyfeeassigned CASCADE;


ALTER TABLE moneycecasefeeassigned 

	ADD COLUMN assignedby_userid 				INTEGER CONSTRAINT moneycecasefeeassigned_assignedby_fk REFERENCES login (userid),
  	ADD COLUMN assignedbyts 					TIMESTAMP WITH TIME ZONE,
  	ADD COLUMN waivedby_userid		 			INTEGER CONSTRAINT moneycecasefeeassigned_wavedbyuserid_fk REFERENCES login (userid),
  	ADD COLUMN lastmodifiedts 					TIMESTAMP WITH TIME ZONE,
  	ADD COLUMN reduceby 						MONEY,
  	ADD COLUMN reduceby_userid					INTEGER CONSTRAINT moneycecasefeeassigned_reducedbyuserid_fk REFERENCES login (userid),
  	ADD COLUMN notes 							text,
	ADD COLUMN fee_feeid integer NOT NULL,
  	ADD COLUMN codesetelement_elementid integer NOT NULL;

ALTER TABLE moneycecasefeeassigned ADD 
	CONSTRAINT moneycecasefeeassigned_feeid_occtypeid_comp_fk FOREIGN KEY (fee_feeid, codesetelement_elementid)
	REFERENCES moneycodesetelementfee (fee_feeid, codesetelement_elementid);


ALTER TABLE moneyoccperiodfeeassigned 

	ADD COLUMN assignedby_userid 				INTEGER CONSTRAINT moneycodesetelementfee_assignedby_fk REFERENCES login (userid),
  	ADD COLUMN assignedbyts 					TIMESTAMP WITH TIME ZONE,
  	ADD COLUMN waivedby_userid		 			INTEGER CONSTRAINT moneycodesetelementfee_wavedbyuserid_fk REFERENCES login (userid),
  	ADD COLUMN lastmodifiedts 					TIMESTAMP WITH TIME ZONE,
  	ADD COLUMN reduceby 						MONEY,
  	ADD COLUMN reduceby_userid					INTEGER CONSTRAINT moneycodesetelementfee_reducedbyuserid_fk REFERENCES login (userid),
  	ADD COLUMN notes 							text,
	ADD COLUMN fee_feeid integer NOT NULL,
  	ADD COLUMN occperiodtype_typeid integer NOT NULL;

ALTER TABLE moneyoccperiodfeeassigned ADD 
	CONSTRAINT moneyoccperiodfeeassigned_feeid_occtypeid_comp_fk FOREIGN KEY (fee_feeid, occperiodtype_typeid)
	REFERENCES moneyoccperiodtypefee (fee_feeid, occperiodtype_typeid);

ALTER TABLE moneyoccperiodfeeassigned RENAME CONSTRAINT moneycodesetelementfee_assignedby_fk TO moneyoccperiodfeeassigned_assignedby_fk;
ALTER TABLE moneyoccperiodfeeassigned RENAME CONSTRAINT moneycodesetelementfee_wavedbyuserid_fk TO moneyoccperiodfeeassigned_waivedbyuserid_fk;
ALTER TABLE moneyoccperiodfeeassigned RENAME CONSTRAINT moneycodesetelementfee_reducedbyuserid_fk TO moneyoccperiodfeeassigned_reducedby_fk;

INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (17, 'database/patches/dbpatch_beta17.sql', '07-03-2019', 'ecd', 'revision to payment system');

