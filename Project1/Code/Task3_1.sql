set default_transaction_isolation = 'read committed';

begin transaction;
update college set college_chinese_name='阿兹卡班2' where college_id=10;
commit transaction ;