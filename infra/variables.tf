variable "project_name" {
  description = "Project name used in AWS resource tags."
  type        = string
  default     = "ws-devops-todo-api"
}

variable "environment" {
  description = "Deployment environment."
  type        = string
  default     = "prod"
}

variable "aws_region" {
  description = "AWS region for all resources."
  type        = string
  default     = "ap-south-1"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC."
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "Public subnet CIDRs. At least two are recommended for ALB high availability."
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]

  validation {
    condition     = length(var.public_subnet_cidrs) >= 2
    error_message = "At least two public subnet CIDRs are required because ALB must span multiple Availability Zones."
  }
}

variable "instance_type" {
  description = "EC2 instance type for the application host."
  type        = string
  default     = "t3.micro"
}

variable "instance_volume_size" {
  description = "Root volume size in GB."
  type        = number
  default     = 20
}

variable "key_name" {
  description = "Existing AWS key pair name for SSH access."
  type        = string
}

variable "allowed_ssh_cidr" {
  description = "CIDR allowed to SSH into the EC2 instance."
  type        = string
  default     = "0.0.0.0/0"
}

variable "app_port" {
  description = "Port exposed by the Spring Boot container."
  type        = number
  default     = 8080
}

variable "app_image" {
  description = "Docker image pulled by EC2 during bootstrap and CI/CD deployments."
  type        = string
  default     = "your-dockerhub-user/ws-devops-todo-api:latest"
}

variable "postgres_db" {
  description = "PostgreSQL database name."
  type        = string
  sensitive   = true
}

variable "postgres_user" {
  description = "PostgreSQL username."
  type        = string
  sensitive   = true
}

variable "postgres_password" {
  description = "PostgreSQL password."
  type        = string
  sensitive   = true
}

variable "jwt_secret" {
  description = "Base64-encoded JWT secret for the Spring Boot application."
  type        = string
  sensitive   = true
}

variable "jwt_expiration_minutes" {
  description = "JWT expiration in minutes."
  type        = number
  default     = 60
}

variable "admin_emails" {
  description = "Emails that should be granted ADMIN role on registration."
  type        = list(string)
  default     = ["admin@ws.local"]
}

variable "cors_allowed_origins" {
  description = "Origins allowed by Spring Security CORS configuration."
  type        = list(string)
  default     = ["http://localhost:3000", "http://localhost:8080"]
}
