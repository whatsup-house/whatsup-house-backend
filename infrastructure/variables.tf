variable "aws_region" {
  description = "AWS 리전"
  type        = string
  default     = "ap-northeast-2"
}

variable "instance_type" {
  description = "EC2 인스턴스 타입 (프리티어: t2.micro)"
  type        = string
  default     = "t2.micro"
}

variable "rds_instance_class" {
  description = "RDS 인스턴스 클래스 (프리티어: db.t3.micro)"
  type        = string
  default     = "db.t3.micro"
}

variable "db_name" {
  description = "데이터베이스 이름"
  type        = string
  default     = "whatsup"
}

variable "db_username" {
  description = "데이터베이스 유저명"
  type        = string
}

variable "db_password" {
  description = "데이터베이스 비밀번호"
  type        = string
  sensitive   = true
}

variable "key_pair_name" {
  description = "EC2 키페어 이름 (AWS 콘솔에서 미리 생성 필요)"
  type        = string
}
