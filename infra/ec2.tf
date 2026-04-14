data "aws_ssm_parameter" "ubuntu_ami" {
  name = "/aws/service/canonical/ubuntu/server/22.04/stable/current/amd64/hvm/ebs-gp3/ami-id"
}

resource "aws_instance" "app" {
  ami                         = data.aws_ssm_parameter.ubuntu_ami.value
  instance_type               = var.instance_type
  subnet_id                   = aws_subnet.public[0].id
  vpc_security_group_ids      = [aws_security_group.ec2.id]
  key_name                    = var.key_name
  associate_public_ip_address = true
  user_data_replace_on_change = true

  metadata_options {
    http_endpoint = "enabled"
    http_tokens   = "required"
  }

  root_block_device {
    volume_size           = var.instance_volume_size
    volume_type           = "gp3"
    delete_on_termination = true
  }

  user_data = <<-EOF
    #!/bin/bash
    set -euxo pipefail

    apt-get update -y
    apt-get install -y ca-certificates curl gnupg lsb-release docker.io
    systemctl enable docker
    systemctl start docker
    usermod -aG docker ubuntu

    mkdir -p /opt/ws-devops-todo-api

    cat > /opt/ws-devops-todo-api/.env <<ENVFILE
    APP_PORT=${var.app_port}
    POSTGRES_DB=${var.postgres_db}
    POSTGRES_USER=${var.postgres_user}
    POSTGRES_PASSWORD=${var.postgres_password}
    JWT_SECRET=${var.jwt_secret}
    JWT_EXPIRATION_MINUTES=${var.jwt_expiration_minutes}
    APP_ADMIN_EMAILS=${join(",", var.admin_emails)}
    APP_CORS_ALLOWED_ORIGINS=${join(",", var.cors_allowed_origins)}
    DOCKER_IMAGE=${var.app_image}
    ENVFILE

    chown -R ubuntu:ubuntu /opt/ws-devops-todo-api

    docker network inspect ws-todo-network >/dev/null 2>&1 || docker network create ws-todo-network
    docker volume inspect ws-todo-postgres-data >/dev/null 2>&1 || docker volume create ws-todo-postgres-data

    if ! docker ps -a --format '{{.Names}}' | grep -q '^ws-todo-postgres$'; then
      docker run -d \
        --name ws-todo-postgres \
        --restart unless-stopped \
        --network ws-todo-network \
        -e POSTGRES_DB='${var.postgres_db}' \
        -e POSTGRES_USER='${var.postgres_user}' \
        -e POSTGRES_PASSWORD='${var.postgres_password}' \
        -v ws-todo-postgres-data:/var/lib/postgresql/data \
        postgres:16-alpine
    else
      docker start ws-todo-postgres >/dev/null 2>&1 || true
    fi

    until docker exec ws-todo-postgres pg_isready -U '${var.postgres_user}' -d '${var.postgres_db}'; do
      sleep 3
    done

    if docker pull '${var.app_image}'; then
      docker rm -f ws-devops-todo-api >/dev/null 2>&1 || true
      docker run -d \
        --name ws-devops-todo-api \
        --restart unless-stopped \
        --network ws-todo-network \
        -p ${var.app_port}:8080 \
        -e SPRING_PROFILES_ACTIVE=prod \
        -e SPRING_DATASOURCE_URL='jdbc:postgresql://ws-todo-postgres:5432/${var.postgres_db}' \
        -e SPRING_DATASOURCE_USERNAME='${var.postgres_user}' \
        -e SPRING_DATASOURCE_PASSWORD='${var.postgres_password}' \
        -e JWT_SECRET='${var.jwt_secret}' \
        -e JWT_EXPIRATION_MINUTES='${var.jwt_expiration_minutes}' \
        -e APP_ADMIN_EMAILS='${join(",", var.admin_emails)}' \
        -e APP_CORS_ALLOWED_ORIGINS='${join(",", var.cors_allowed_origins)}' \
        '${var.app_image}'
    else
      echo "Docker image ${var.app_image} was not available during provisioning. Instance is ready for CI/CD deployment."
    fi
  EOF

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-ec2"
  })
}
