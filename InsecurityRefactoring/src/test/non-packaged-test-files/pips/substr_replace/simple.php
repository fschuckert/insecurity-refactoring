<?php


$a = $_GET;

$b = substr_replace($a, "lala", 2, 3);

$c = substr_replace($a, "lala", 2);

echo($c);

?>
