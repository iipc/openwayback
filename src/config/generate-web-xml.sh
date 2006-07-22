#!/usr/bin/env bash
primary=primary.txt
head=head.xml
tail=tail.xml

if [ -z "$1" ]; then
    final_path=/tmp/wayback-config
else
    final_path=$1
fi

main_target=$final_path/web.xml

[ -d $final_path ] || mkdir -pv $final_path
echo CLEANING UP OLD FINALS:
find "$final_path/" -type f -print0 | xargs -0 -r rm -fv

function trim_file () {
    local basen=$1
    local prefix=$2
    local trimmed=$(echo $basen | sed -e "s@.xml\$@@; s@^$prefix-@@;")
    echo $trimmed
}

function get_header () {
    local file=$1
    grep ^HEADER: $file.comment | cut -d ' ' -f 2- | sed -e 's@ *$@@; s@^ *@@;'
}
function get_comment () {
    local file=$1
    grep -v ^HEADER: $file.comment | sed -e 's@ *$@@;'
}
function is_primary () {
    local file=$1
    grep $file $primary | wc -l
}

function make_primary_section () {
    local file=$1
    local file_header=$(get_header $file)
    echo '<!-- START OF ' $file_header ' OPTIONS'
    get_comment $file
    echo '-->'
    cat $file
    echo '<!-- END OF ' $file_header ' OPTIONS -->'
    echo
}

function make_commented_section () {
    local file=$1
    local file_header=$(get_header $file)
    echo '<!-- START OF ' $file_header ' OPTIONS'
    get_comment $file
    echo
    echo 'These options are not used by default.'
    echo '-->'
    echo '<!--'
    cat $file
    echo '-->'
    echo '<!-- END OF ' $file_header ' OPTIONS -->'
    echo
}

function make_sections () {
    local section_name=$1
    shift
    echo "<!--"
    echo "       $section_name SECTION"
    echo "-->"
    for i in $*; do
	if [ $(is_primary $i) -ne 0 ]; then
	    make_primary_section $i
	fi
    done
    for i in $*; do
	if [ $(is_primary $i) -eq 0 ]; then
	    make_commented_section $i
	fi
    done
    echo "<!--"
    echo "       END OF $section_name SECTION"
    echo "-->"
    echo
    echo
}


# FIRST GENERATE THE "AGGREGATE" COMBINED VERSION:

sed -e "s@TEMPLATE@General Configuration@;" < $head > $main_target

make_sections "USER INTERFACE" ui-*.xml >> $main_target
make_sections "RESOURCE STORE" rs-*.xml >> $main_target
make_sections "RESOURCE INDEX" ri-*.xml >> $main_target
make_sections "RESOURCE INDEX EXCLUSION" ex-*.xml >> $main_target

cat $tail >> $main_target


# NOW GENERATE ALL THE PERMUTATIONS:

for ext in ex-*.xml; do
    exb=$(trim_file $ext ex)

    for rit in ri-*.xml; do
	rib=$(trim_file $rit ri)

	for rst in rs-*.xml; do
	    rsb=$(trim_file $rst rs)

	    for uit in ui-*.xml; do
		uib=$(trim_file $uit ui)

		target=$(printf "%s/web-%s-%s-%s-%s.xml" $final_path $uib $rsb $rib $exb)
		echo -n Generating $target...
		pretty_name=$(for i in $uit $rst $rit $ext; do echo -n $(get_header $i) ", "; done)
		pretty_name=$(echo $pretty_name | sed -e 's@, *$@@;')
		sed -e "s@TEMPLATE@$pretty_name@;" < $head > $target
		for i in $uit $rst $rit $ext; do
		    echo '<!--  START OF ' $(get_header $i) ' CONFIGURATION'
		    get_comment $i
		    echo '-->'
		    cat $i
		    echo '<!-- END OF ' $(get_header $i) ' CONFIGURATION -->'
		    echo
		done >> $target
		cat $tail >> $target
		echo Done.
	    done
	done
    done
done

