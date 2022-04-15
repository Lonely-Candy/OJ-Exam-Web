package com.smxy.exam.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author fsy
 * @since 2022-02-10
 */
@Data
@Accessors(chain = true)
@TableName("exam_procedure_bank")
public class ExamProcedureBank implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 题号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 标题
     */
    private String title;

    /**
     * 题干
     */
    private String content;

    /**
     * 填空代码
     */
    private String code;

    /**
     * 参考答案，每个空用#隔开
     */
    private String answer;

    /**
     * 得分，每个空用#隔开
     */
    private String score;

    /**
     * 编辑器
     */
    private String compile;

    /**
     * 创建人
     */
    private String adminid;

    /**
     * 创建时间
     */
    private LocalDateTime time;

    /**
     * 判断是否包含该编译器
     *
     * @param compile 待判断的编译器
     * @return boolean
     * @author 范颂扬
     * @date 2022-04-12 15:17
     */
    public boolean isExistCompile(String compile) {
        String[] compiles = this.compile.split("#");
        for (int i = 0; i < compiles.length; i++) {
            if (compiles[i].equals(compile)) {
                return true;
            }
        }
        return false;
    }


}
