<?php

function add($b, $c)
{
	$d = $b + $c;

	if($d < 100){
		return add($d, 10);
	}

	return $d;
}

$tainted = $_GET['tainted'];


$tainted = add($tainted, "blubb");


while(true) {
	$tainted = $tainted . "bla";
}

echo $tainted;



?>
