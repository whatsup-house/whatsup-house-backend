output "ec2_public_ip" {
  description = "EC2 퍼블릭 IP → GitHub Secrets EC2_HOST에 등록"
  value       = aws_eip.app.public_ip
}

output "rds_endpoint" {
  description = "RDS 엔드포인트"
  value       = aws_db_instance.postgres.endpoint
}

output "db_url" {
  description = "GitHub Secrets DB_URL에 등록"
  value       = "jdbc:postgresql://${aws_db_instance.postgres.endpoint}/${var.db_name}"
}
