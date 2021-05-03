<?php

$v = $_GET;

$b = $v;


// from this
$a = $b['tainted'];

// to this
$a = null;
foreach($b as $k => $c )
{
	if($k == 'tainted'){
		$a = $c;
	}
}
//----

echo $a;



?>
