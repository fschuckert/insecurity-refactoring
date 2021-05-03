<?php

function add($b, $c)
{
	return $b . $c;
}

function bar($c)
{
	return $c;
}

$tainted = $_GET['tainted'];

if(is_numeric($tainted))
{
	$t2 = add("lala", bar($tainted));
	echo $t2;
}





?>
