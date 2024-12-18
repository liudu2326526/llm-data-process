DROP FUNCTION prod_function.baidu_html_parser;

CREATE FUNCTION prod_function.baidu_html_parser AS 'llm.hive.udf.BaiduHtmlParser'
    using jar 'obs://donson-mip-data-warehouse/jar/udf/llm-data-process-1.0-SNAPSHOT.jar';

