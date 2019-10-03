select crypt('snewpass', gen_salt('md5'));
SELECT convert_to('test', 'UTF8');

UPDATE login SET password = crypt('newpass', gen_salt('md5')) WHERE userid = 100;

UPDATE login SET password = encode(digest('ten', 'md5'), 'base64') WHERE userid = 100;

select encode(digest('another', 'md5'));


SELECT (password = crypt('newpass3', password)) as matchresult FROM login WHERE userid = 100;

SELECT convert_from('Im in utf9', 'UTF8');

select encode(digest('teestdigestme', 'md5'), 'base64');

                            <module-option name="hashAlgorithm" value="MD5"/>
                            <module-option name="hashEncoding" value="base64"/>
ALTER TABLE login DROP COLUMN userrole;

ALTER TYPE role ADD VALUE IF NOT EXISTS 'User' AFTER 'Public';
ALTER TABLE login ADD COLUMN userrole role DEFAULT 'User'::role ;

UPDATE login SET userrole = 'User'::role;

SELECT text(userrole), 'Roles' FROM login WHERE username='edarsow';