<?php



class Decoder
{
    const TYPE_ARRAY  = 1;


    public static function decode($encodedValue, $objectDecodeType = Decoder::TYPE_ARRAY)
    {
        $encodedValue = (string) $encodedValue;
        if (function_exists('json_decode')) {
            $decode = json_decode($encodedValue, $objectDecodeType);

            // php < 5.3
            if (!function_exists('json_last_error')) {
                if (strtolower($encodedValue) === 'null') {
                    return null;
                } elseif ($decode === null) {
                    // require_once 'Zend/Json/Exception.php';
                    throw new Zend_Json_Exception('Decoding failed');
                }
            }

            return $decode;
        }

        return "Error";
    }
}

?>