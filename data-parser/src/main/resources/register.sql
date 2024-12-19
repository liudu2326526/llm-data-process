DROP FUNCTION prod_function.baidu_html_parser;

CREATE FUNCTION prod_function.baidu_html_parser AS 'com.hive.udf.BaiduHtmlParser'
    using jar 'obs://donson-mip-data-warehouse/jar/udf/data-parser-1.0.0.jar';

DROP FUNCTION prod_function.maigou_html_parser;

CREATE FUNCTION prod_function.maigou_html_parser AS 'com.hive.udf.MaigouHtmlParser'
    using jar 'obs://donson-mip-data-warehouse/jar/udf/data-parser-1.0.0.jar';
