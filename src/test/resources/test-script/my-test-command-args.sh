#!/bin/bash

# 初始化参数标志
M_FLAG=false
N_FLAG=false
M_VALUE=""
N_VALUE=""

# 显示使用方法
show_usage() {
    echo "用法: $SCRIPT_NAME [-m value] [-n value] [-h]"
    echo ""
    echo "选项:"
    echo "  -m VALUE    指定m参数的值"
    echo "  -n VALUE    指定n参数的值"
    echo "  -h          显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $SCRIPT_NAME -m test1 -n test2"
    echo "  $SCRIPT_NAME -m \"hello world\" -n 123"
}

# 解析命令行参数
while getopts "m:n:h" opt; do
    case "$opt" in
        m)
            M_FLAG=true
            M_VALUE="$OPTARG"
            echo "检测到 -m 参数，值为: $M_VALUE"
            ;;
        n)
            N_FLAG=true
            N_VALUE="$OPTARG"
            echo "检测到 -n 参数，值为: $N_VALUE"
            ;;
        h)
            show_usage
            exit 0
            ;;
        \?)
            echo "错误: 无效的选项 -$OPTARG" >&2
            show_usage
            exit 1
            ;;
        :)
            echo "错误: 选项 -$OPTARG 需要参数" >&2
            show_usage
            exit 1
            ;;
    esac
done

# 移除已处理的参数
shift $((OPTIND-1))

# 检查是否提供了必需的参数
if [ "$M_FLAG" = false ] && [ "$N_FLAG" = false ]; then
    echo "警告: 没有提供 -m 或 -n 参数"
    show_usage
    exit 1
fi

# 主要业务逻辑
echo "========== 脚本执行开始 =========="
echo "执行时间: $(date)"
echo ""

if [ "$M_FLAG" = true ]; then
    echo "-m 参数值为: $M_VALUE"
fi

if [ "$N_FLAG" = true ]; then
    echo "-n 参数值为: $N_VALUE"
fi

echo ""
echo "========== 脚本执行完成 =========="

exit 0
