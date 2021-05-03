<?php

if(true){
	$tainted = $_GET['tainted'];
}
else {
	$tainted = "Default";
}


$sanitized = htmlspecialchars($tainted, ENT_QUOTES);

$output = "<script> alert('$sanitized'); </script>";



echo $output;



?>
