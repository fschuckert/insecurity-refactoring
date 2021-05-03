<?php

if(true){
    $a = "Hallo '";
}
else {
    $a = "Hi '";
}

$b = $_GET['lala'];
$c = "'";

$d = $a . $b. $c;

echo($d);

?>
