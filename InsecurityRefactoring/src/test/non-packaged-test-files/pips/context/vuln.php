<?php

$pre = "Hello";

if(true){
	$pre .= " you ";
}
else{
	$pre .= " me ";
}

$s = $_GET['tainted'];

if(true){
	$b = $s;
}
else{
	$b = "default";
}


echo $pre . $b;



?>
