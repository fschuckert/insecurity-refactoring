<?php

function getParam(){
    return $_GET['input'];
}

$array = [];

$tainted = call_user_func_array('getParam', $array);

echo($tainted);
?>