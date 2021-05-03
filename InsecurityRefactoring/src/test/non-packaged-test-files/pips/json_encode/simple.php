<?php


$a = $_GET['lala'];

$b = json_encode($a);

$c = json_encode($b, 1);

$d = json_encode($c, 1, 256);

echo($d);

?>
