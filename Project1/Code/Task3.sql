
update college set college_chinese_name='阿兹卡班' where college_chinese_name='阿兹卡班2';
begin;
select * from college where college_chinese_name='阿兹卡班2';
commit ;