package com.smxy.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ExamApplicationTests {

    @Test
    void testGenerator() {
        FastAutoGenerator.create("jdbc:mysql://localhost:3307/smuoj"
                , "root", "fsy123")
                .globalConfig(builder -> {
                    builder.author("fsy").fileOverride()
                            .outputDir("D:\\Projcets\\ideaProjects\\exam\\src\\main\\java"); })
                .packageConfig(builder -> {
                    builder.parent("com.smxy.exam").mapper("mapper").service("service")
                            .controller("controller").entity("beans").xml("mapper.xml"); })
                .strategyConfig(builder -> {
                    builder.addInclude("admin", "user", "exam"
                            , "exam_completion_bank", "exam_completion_problem"
                            , "exam_completion_status"
                            , "exam_procedure_bank", "exam_procedure_problem"
                            , "exam_procedure_status"
                            , "exam_record")
                            .entityBuilder().enableLombok().enableChainModel()
                            .controllerBuilder().enableRestStyle()
                            .mapperBuilder().enableMapperAnnotation();
                }).execute();
    }

}
