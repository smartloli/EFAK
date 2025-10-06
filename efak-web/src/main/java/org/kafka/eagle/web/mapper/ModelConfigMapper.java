package org.kafka.eagle.web.mapper;

import org.apache.ibatis.annotations.*;
import org.kafka.eagle.dto.config.ModelConfig;
import org.kafka.eagle.dto.config.ModelConfigStatisticsDTO;

import java.util.List;

/**
 * <p>
 * 大模型配置Mapper接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/15 23:32:21
 * @version 5.0.0
 */
@Mapper
public interface ModelConfigMapper {

        /**
         * 分页查询模型配置列表
         */
        @Select("<script>" +
                        "SELECT id, model_name, api_type, endpoint, api_key, system_prompt, timeout, description, " +
                        "enabled, status, create_time, update_time, create_by, update_by " +
                        "FROM ke_model_config " +
                        "<where>" +
                        "<if test='search != null and search != \"\"'>" +
                        "AND (model_name LIKE CONCAT('%', #{search}, '%') " +
                        "OR api_type LIKE CONCAT('%', #{search}, '%') " +
                        "OR description LIKE CONCAT('%', #{search}, '%'))" +
                        "</if>" +
                        "<if test='apiType != null and apiType != \"\"'>" +
                        "AND api_type = #{apiType}" +
                        "</if>" +
                        "<if test='enabled != null'>" +
                        "AND enabled = #{enabled}" +
                        "</if>" +
                        "</where>" +
                        "ORDER BY create_time DESC " +
                        "<if test='offset != null and limit != null'>" +
                        "LIMIT #{offset}, #{limit}" +
                        "</if>" +
                        "</script>")
        List<ModelConfig> selectModelConfigList(@Param("search") String search,
                        @Param("apiType") String apiType,
                        @Param("enabled") Integer enabled,
                        @Param("offset") Integer offset,
                        @Param("limit") Integer limit);

        /**
         * 查询模型配置总数
         */
        @Select("<script>" +
                        "SELECT COUNT(*) FROM ke_model_config " +
                        "<where>" +
                        "<if test='search != null and search != \"\"'>" +
                        "AND (model_name LIKE CONCAT('%', #{search}, '%') " +
                        "OR api_type LIKE CONCAT('%', #{search}, '%') " +
                        "OR description LIKE CONCAT('%', #{search}, '%'))" +
                        "</if>" +
                        "<if test='apiType != null and apiType != \"\"'>" +
                        "AND api_type = #{apiType}" +
                        "</if>" +
                        "<if test='enabled != null'>" +
                        "AND enabled = #{enabled}" +
                        "</if>" +
                        "</where>" +
                        "</script>")
        Long selectModelConfigCount(@Param("search") String search,
                        @Param("apiType") String apiType,
                        @Param("enabled") Integer enabled);

        /**
         * 根据ID查询模型配置
         */
        @Select("SELECT id, model_name, api_type, endpoint, api_key, system_prompt, timeout, description, " +
                        "enabled, status, create_time, update_time, create_by, update_by " +
                        "FROM ke_model_config WHERE id = #{id}")
        ModelConfig selectModelConfigById(@Param("id") Long id);

        /**
         * 根据模型名称查询模型配置
         */
        @Select("SELECT id, model_name, api_type, endpoint, api_key, system_prompt, timeout, description, " +
                        "enabled, status, create_time, update_time, create_by, update_by " +
                        "FROM ke_model_config WHERE model_name = #{modelName}")
        ModelConfig selectModelConfigByName(@Param("modelName") String modelName);

        /**
         * 插入模型配置
         */
        @Insert("INSERT INTO ke_model_config (" +
                        "model_name, api_type, endpoint, api_key, system_prompt, timeout, description, " +
                        "enabled, status, create_by, update_by" +
                        ") VALUES (" +
                        "#{modelName}, #{apiType}, #{endpoint}, #{apiKey}, #{systemPrompt}, #{timeout}, #{description}, " +
                        "#{enabled}, #{status}, #{createBy}, #{updateBy}" +
                        ")")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insertModelConfig(ModelConfig modelConfig);

        /**
         * 更新模型配置
         */
        @Update("<script>" +
                        "UPDATE ke_model_config " +
                        "<set>" +
                        "<if test='modelName != null'>model_name = #{modelName},</if>" +
                        "<if test='apiType != null'>api_type = #{apiType},</if>" +
                        "<if test='endpoint != null'>endpoint = #{endpoint},</if>" +
                        "<if test='apiKey != null'>api_key = #{apiKey},</if>" +
                        "<if test='systemPrompt != null'>system_prompt = #{systemPrompt},</if>" +
                        "<if test='timeout != null'>timeout = #{timeout},</if>" +
                        "<if test='description != null'>description = #{description},</if>" +
                        "<if test='enabled != null'>enabled = #{enabled},</if>" +
                        "<if test='status != null'>status = #{status},</if>" +
                        "<if test='updateBy != null'>update_by = #{updateBy},</if>" +
                        "update_time = NOW()" +
                        "</set>" +
                        "WHERE id = #{id}" +
                        "</script>")
        int updateModelConfig(ModelConfig modelConfig);

        /**
         * 删除模型配置
         */
        @Delete("DELETE FROM ke_model_config WHERE id = #{id}")
        int deleteModelConfigById(@Param("id") Long id);

        /**
         * 更新模型状态
         */
        @Update("UPDATE ke_model_config SET status = #{status}, update_time = NOW() WHERE id = #{id}")
        int updateModelStatus(@Param("id") Long id, @Param("status") Integer status);

        /**
         * 查询所有启用的模型配置
         */
        @Select("SELECT id, model_name, api_type, endpoint, api_key, system_prompt, timeout, description, " +
                        "enabled, status, create_time, update_time, create_by, update_by " +
                        "FROM ke_model_config WHERE enabled = 1 ORDER BY create_time ASC")
        List<ModelConfig> selectEnabledModelConfigs();

        /**
         * 查询模型配置统计数据
         */
        @Select("SELECT " +
                        "COUNT(*) as totalModels, " +
                        "COUNT(CASE WHEN status = 1 THEN 1 END) as onlineModels, " +
                        "COUNT(CASE WHEN status = 0 THEN 1 END) as offlineModels, " +
                        "COUNT(CASE WHEN enabled = 0 THEN 1 END) as disabledModels, " +
                        "COUNT(CASE WHEN enabled = 1 THEN 1 END) as enabledModels, " +
                        "COUNT(CASE WHEN status = 2 THEN 1 END) as errorModels, " +
                        "COUNT(CASE WHEN api_type = 'OpenAI' THEN 1 END) as openaiModels, " +
                        "COUNT(CASE WHEN api_type = 'Ollama' THEN 1 END) as ollamaModels, " +
                        "COUNT(CASE WHEN api_type = 'DeepSeek' THEN 1 END) as deepseekModels, " +
                        "COUNT(CASE WHEN api_type NOT IN ('OpenAI', 'Ollama', 'DeepSeek') THEN 1 END) as otherModels " +
                        "FROM ke_model_config")
        ModelConfigStatisticsDTO selectModelConfigStatistics();
}