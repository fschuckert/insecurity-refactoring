<?php

function PMA_isValid(&$var, $type = 'length', $compare = null)
{
    if (gettype($var) === $type) {
        return true;
    }

    return false;
}


?>