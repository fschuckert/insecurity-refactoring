<?php
/*
$oidAs = $_POST["OID_AS"];
$TEST = $oidAs;
echo $TEST;
*/

/*
function foo($a) {
    shell_exec($a);     
}
*/

/*
function foo($a, $b) {
    shell_exec($a);
    shell_exec($b);   
}
*/

function foo1(){}

function foo2($a){}

function foo2($a, $b){
shell_exec($a);
shell_exec($b);
shell_exec($a);
}


?>
