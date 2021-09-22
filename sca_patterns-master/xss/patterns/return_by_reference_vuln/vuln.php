<?php

$arr = array('useless', $_GET['input'] );

[$useless, $tainted]= $arr; 





echo($tainted);


?>