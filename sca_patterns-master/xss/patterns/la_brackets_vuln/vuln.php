<?php

function getParam(&$ref){
    $ref = $_GET['input'];
    return true;
}

$tainted;

if( getParam($tainted)){
    echo($tainted);
}


?>