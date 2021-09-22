<?php

$tainted = $_GET['input'];

$arr = unserialize($tainted);





echo($arr['foo']);


?>