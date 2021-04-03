package org.javahelper.core.object;

import org.apache.commons.compress.utils.Lists;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.springframework.beans.BeanUtils;
import org.springframework.cglib.beans.BeanCopier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Description: 对象操作
 * 果一个对象只包含基本字段属性，可以优先考虑使用BeanCopier，但是这个工具需要依赖CGLIB包，
 * 如果不想依赖，则考虑使用Spring的BeanUtils，如果对象包含自定义类字段属性，再拷贝的时候
 * 推荐使用DozerBeanMapper工具。
 * @author shenguangyang
 * @date 2021/03/31
 */
public class ObjectCopyHelper {

    /**
     * Dozer是一个实现对象间字段赋值转换的工具包。
     * 它支持简单的属性映射，复杂类型映射，双向映射，隐式显式的映射，以及递归映射。
     * 它支持三种映射方式：注解、API、XML。
     * 微服中，有大量实体转换，从前端表单转换为业务实体，从业务实体转换为报表Dto。
     *
     * DozerMapper -- 支持基本数据类型以及Date、枚举、自定义对象类型转换
     * 1.默认会匹配名称相同的属性
     *    若存在两个属性，其中一个名称相同，另一个名称不同，但使用@Mapper指定的名称相同，
     *    则只给@Mapper对应的属性赋值
     *   @see 测试方法：com.java.dozer.controller.DozerMapperTest#modeToDto()
     * 2.名称不同的属性需要赋值，则需要使用@Mapper
     * 3.可以使用@Mapper给嵌套对象赋值
     */
    public static class Dozer {
        /**
         * 单个对象属性拷贝
         *
         * @param source 源对象
         * @param clazz  目标对象Class
         * @param <T>    目标对象类型
         * @param <M>    源对象类型
         * @return 目标对象
         */
        public static <T, M> T copyProperties(M source, Class<T> clazz) {
            if (Objects.isNull(source) || Objects.isNull(clazz)) {
                throw new IllegalArgumentException();
            }
            Mapper mapper = BeanHolder.MAPPER.getMapper();
            return mapper.map(source, clazz);
        }

        /**
         * 列表对象拷贝
         *
         * @param sources 源列表
         * @param clazz   源列表对象Class
         * @param <T>     目标列表对象类型
         * @param <M>     源列表对象类型
         * @return 目标列表
         */
        public static <T, M> List<T> copyObjects(List<M> sources, Class<T> clazz) {
            if (Objects.isNull(sources) || Objects.isNull(clazz) || sources.isEmpty()) {
                throw new IllegalArgumentException();
            }
            return Optional.of(sources)
                    .orElse(new ArrayList<>())
                    .stream().map(m -> copyProperties(m, clazz))
                    .collect(Collectors.toList());
        }

        /**
         * 单例
         * <p>
         * DozerBeanMapper使用单例，有利于提高程序性能
         */
        private enum BeanHolder {
            MAPPER;
            private DozerBeanMapper mapper;
            BeanHolder() {
                this.mapper = new DozerBeanMapper();
            }
            public DozerBeanMapper getMapper() {
                return mapper;
            }
        }
    }

    /**
     * 单个对象属性拷贝、列表对象拷贝
     * 需要明确说的是，BeanCopier是浅拷贝，如需使用深拷贝
     */
    public static class Copier {
        /**
         * 单个对象属性拷贝
         * @param source 源对象
         * @param clazz 目标对象Class
         * @param <T> 目标对象类型
         * @param <M> 源对象类型
         * @return 目标对象
         */
        public static <T, M> T copyProperties(M source, Class<T> clazz){
            if (Objects.isNull(source) || Objects.isNull(clazz)) {
                throw new IllegalArgumentException();
            }
            return copyProperties(source, clazz, null);
        }

        /**
         * 列表对象拷贝
         * @param sources 源列表
         * @param clazz 源列表对象Class
         * @param <T> 目标列表对象类型
         * @param <M> 源列表对象类型
         * @return 目标列表
         */
        public static <T, M> List<T> copyObjects(List<M> sources, Class<T> clazz) {
            if (Objects.isNull(sources) || Objects.isNull(clazz) || sources.isEmpty()) {
                throw new IllegalArgumentException();
            }
            BeanCopier copier = BeanCopier.create(sources.get(0).getClass(), clazz, false);
            return Optional.of(sources)
                    .orElse(new ArrayList<>())
                    .stream().map(m -> copyProperties(m, clazz, copier))
                    .collect(Collectors.toList());
        }

        /**
         * 单个对象属性拷贝
         * @param source 源对象
         * @param clazz 目标对象Class
         * @param copier copier
         * @param <T> 目标对象类型
         * @param <M> 源对象类型
         * @return 目标对象
         */
        private static <T, M> T copyProperties(M source, Class<T> clazz, BeanCopier copier){
            if (null == copier){
                copier = BeanCopier.create(source.getClass(), clazz, false);
            }
            T t = null;
            try {
                t = clazz.newInstance();
                copier.copy(source, t, null);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return t;
        }
    }

    /**
     * 写Java的同学应该都知道，在Java里面有各种O（PO，VO，TO，QO，BO，DTO），
     * 我们经常需要将各种O对象之间转换数据，用的比较多的就是Spring的BeanUtils
     * 工具的copyProperties函数和dozer的Mapper，这两种都可以完成属性的复制，
     * 但是无法完成列表的快速复制，因而笔者封装了一下，来支持列表的复制。
     *
     * 需要明确说的是，BeanUtils是浅拷贝，如需使用深拷贝
     */
    public static class Bean {
        /**
         * 单个对象属性复制
         *
         * @param source 复制源
         * @param clazz  目标对象class
         * @param <T>    目标对象类型
         * @param <M>    源对象类型
         * @return 目标对象
         */
        public static <T, M> T copyProperties(M source, Class<T> clazz) {
            if (Objects.isNull(source) || Objects.isNull(clazz)) {
                throw new IllegalArgumentException();
            }
            T t = null;
            try {
                t = clazz.newInstance();
                BeanUtils.copyProperties(source, t);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return t;
        }

        /**
         * 列表对象属性复制
         *
         * @param sources 源对象列表
         * @param clazz   目标对象class
         * @param <T>     目标对象类型
         * @param <M>     源对象类型
         * @return 目标对象列表
         */
        public static <T, M> List<T> copyObjects(List<M> sources, Class<T> clazz) {
            if (Objects.isNull(sources) || Objects.isNull(clazz)) {
                throw new IllegalArgumentException();
            }
            return Optional.of(sources)
                    .orElse(Lists.newArrayList())
                    .stream().map(m -> copyProperties(m, clazz))
                    .collect(Collectors.toList());
        }
    }
}
