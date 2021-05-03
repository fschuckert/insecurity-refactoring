<?php

$a = '';

if($_GET['bool'] == 'yes')
{
	$a = $_GET['tainted'];
}
else 
{
	$a = 'lala';
}

echo $a;



?>
