<?php

$tainted = "Hello ";

$array = $_GET;


$tainted .= $array['tainted'];

$doesNotReach = $_GET['notreach'];


$t =  htmlspecialchars(htmlspecialchars($tainted, "", ""), ENT_QUOTES, $doesNotReach); // -> $t = $tainted
echo ("<script> $t </script>");



?>
