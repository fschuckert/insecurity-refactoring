<?php

function getParam(){
    return $_GET['input'];
}

$funcName = 'getParam';

$tainted = $funcName();

echo($tainted);
?>