<?php

function add($b, $c)
{
	return $b + $c;
}

$tainted = $_GET['tainted'];

$t2 = $tainted;

$useless1 = add(1, 2);

$t3 = add($t2, "lala");

echo $t3;



?>
