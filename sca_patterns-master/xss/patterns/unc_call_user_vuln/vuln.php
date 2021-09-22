<?php

function getParam(){
    return $_GET['input'];
}



$tainted = call_user_func('getParam');

echo($tainted);
?>