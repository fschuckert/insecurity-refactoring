<?php

class Params
{
    static function getParam($param, $htmlencode = false)
    {
        $value = $_REQUEST[$param];
        if ($htmlencode) {
            return htmlspecialchars(stripslashes($value), ENT_QUOTES);
        }

        if(get_magic_quotes_gpc()) {
            $value = stripslashes($value);
        }

        return $value;
    }
}

?>