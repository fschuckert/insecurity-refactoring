<?php

function foo($a, $b)
{
	$r = "blubb" . $b;
	return $r;
}

$var1 = $_GET['blubb'];

$var2 = foo("lala", $var1); // data flows to var2


echo "hello" . $var2 . foo("jup", $var1);
 
 
echo $var1;



?>
