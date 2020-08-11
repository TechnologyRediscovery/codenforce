-- Creates taxstatus table and links propertyexternaldata to it

CREATE SEQUENCE taxstatus_taxstatusid_seq
    INCREMENT 1
    START 1000
    MINVALUE 1000;

create table if not exists taxstatus(
    taxstatusid         int primary key,
    year                int,
    paidstatus          text,
    tax                 decimal,
    penalty             decimal,
    interest            decimal,
    total               decimal,
    datepaid            date
);
alter table taxstatus
    alter column taxstatusid
    set default nextval('taxstatus_taxstatusid_seq');
comment on table taxstatus IS
'Scraped data from Allegheny County http://www2.alleghenycounty.us/RealEstate/. Description valid as of August 2020';


alter table propertyexternaldata
   add column taxstatus_taxstatusid int references taxstatus(taxstatusid),
   drop column tax,
   drop column taxsubcode,
   drop column taxstatus,
   drop column taxstatusyear;