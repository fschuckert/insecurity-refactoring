<?php 

// No alias of $_GET/... anywhere

function foo($a) {
    eval($a);

    eval($_GET['cmd']);
    
    $b = $_POST['d'];
    $c = $_POST['d'];

    print $c;
    eval($b);
}

?>