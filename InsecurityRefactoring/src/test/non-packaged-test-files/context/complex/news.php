<?php

require_once("simple_function.php");

function getVal(){
	return "be";
}

$a = "News" . $b . getVal();

page(false, $a);

?>
