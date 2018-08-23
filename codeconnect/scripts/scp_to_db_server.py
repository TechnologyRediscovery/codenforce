import paramiko

HOSTNAME = 'capstone1016.heinz.cmu.edu'
USER = 'dsarmien'
PUBLIC_KEY = 'C:\Users\lemon\.ssh\id_rsa.pub'
DESTINATION_DIRECTORY = '/home/dsarmien/dropbox'

FILES_TO_COPY = (
	'property.csv',
	'code_violation.csv',
	'propertyAgent.csv',
	'municipality.csv',
	'propertyOwner.csv',
	'codeEnfCase.csv',
	'codeEnfEvent.csv',
	'codeOfficer.csv',
	'inspecChecklist.csv',
	'occInpsec.csv',
	'occPermit.csv',
	'payment.csv',
)

def main():
	ssh_connection = get_ssh_connection()
	sftp = ssh_connection.open_sftp()
	sftp.chdir(DESTINATION_DIRECTORY)
	for file in FILES_TO_COPY:
		sftp.put(file, file)
	#stdin, stdout, stderr = ssh_connection.exec_command(
	change_line_endings_command = 'fromdos %s/*csv' % DESTINATION_DIRECTORY
	ssh_connection.exec_command(change_line_endings_command)
	ssh_connection.close()


ssh_connection = None
def get_ssh_connection():
	global ssh_connection
	if ssh_connection is not None:
		return ssh_connection
	ssh_connection = paramiko.SSHClient()

	# Avoid known_hosts error
	# TODO: In production, it would be necessary to be more strict avoid server's identity
	ssh_connection.set_missing_host_key_policy(paramiko.AutoAddPolicy())
	ssh_connection.connect(HOSTNAME, username=USER, key_filename=PUBLIC_KEY)

	return ssh_connection

if __name__ == '__main__':
	main()
