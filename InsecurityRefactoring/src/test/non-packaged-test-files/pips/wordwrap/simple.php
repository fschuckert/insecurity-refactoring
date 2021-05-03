<?php


$a = $_GET['lala'];

$b = wordwrap($a);

$c = wordwrap( $b, 20, "<br />\n" );


echo($c);

?>
