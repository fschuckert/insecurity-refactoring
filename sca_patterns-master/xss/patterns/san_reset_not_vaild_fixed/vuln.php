<?php

$tainted = $_GET['tainted'];

if (!is_numeric($tainted))
{
    $tainted="";
}

echo $tainted;

?>