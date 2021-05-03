<?php


$foo = $_GET['blubb'];

if(is_int(1)){
    $foo = "blubb" . $foo;
}
else {
    $foo = "blubb";
}

if(is_int($foo)){
    if($foo > 10){
        echo $foo;
    }
}





?>
