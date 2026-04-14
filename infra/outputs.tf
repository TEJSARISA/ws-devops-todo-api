output "public_ip" {
  description = "Public IP of the EC2 instance."
  value       = aws_instance.app.public_ip
}

output "alb_dns" {
  description = "DNS name of the Application Load Balancer."
  value       = aws_lb.app.dns_name
}

output "cloudfront_url" {
  description = "CloudFront URL in front of the ALB."
  value       = "https://${aws_cloudfront_distribution.api.domain_name}"
}
