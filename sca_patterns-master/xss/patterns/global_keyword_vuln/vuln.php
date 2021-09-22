<?php

function getParam(){
    global $var;
    $var = $_GET['input'];
}


function printGlobal(){
    global $var;
    echo $var;
}

getParam();
printGlobal();
?>