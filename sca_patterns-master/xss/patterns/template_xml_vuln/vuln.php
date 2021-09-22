<?php

$tainted = $_GET['tainted'];

$output = '';
$title = 'target';

# template
$template_xml = simplexml_load_file("template.xml");
$res = $template_xml->xpath("//template[@name='{$title}']")[0];
$str = "\$output .= \"".$res."\";";
eval($str); # critical unsanitized data into a eval function!

#output
print_r($output);

?>