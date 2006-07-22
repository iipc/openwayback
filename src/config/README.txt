The shell script generate-web-xml.sh will create a single configuration file, 
web.xml which includes all options possible, and has all but the default
configurations commented out. We're hoping this will be easier for users to
configure.

Another benefit is that it allows us to keep a single copy of the configuration
and choices for each option, rather than trying to maintain the LocalBDB 
configuration across multiple "starter" web.xml files.

Also, the script will generate a single web-*.xml file for every permutation of
configuration choices, which should simplify testing. If no arguments are 
provided to the script, it will place all generated files in 
/tmp/wayback-config, creating the directory if needed. If an argument is 
provided all files will be placed in the directory argument. The script should 
be run within the config/ directory.