function [Vx,Vy] = compute_LK_optical_flow(frame_1,frame_2,type_LK)

% You have to implement the Lucas Kanade algorithm to compute the
% frame to frame motion field estimates. 
% frame_1 and frame_2 are two gray frames where you are given as inputs to 
% this function and you are required to compute the motion field (Vx,Vy)
% based upon them.
% -----------------------------------------------------------------------%
% YOU MUST SUBMIT ORIGINAL WORK! Any suspected cases of plagiarism or 
% cheating will be reported to the office of the Dean.  
% You CAN NOT use packages that are publicly available on the WEB.
% -----------------------------------------------------------------------%

% There are three variations of LK that you have to implement,
% select the desired alogrithm by passing in the argument as follows:
% "LK_naive", "LK_iterative" or "LK_pyramid"

switch type_LK

    case "LK_naive"
        % YOUR IMPLEMENTATION GOES HERE
        m = 5;
        sigma = 1; 
        Vx = zeros(size(frame_1,1),size(frame_1,2));
        Vy = zeros(size(frame_1,1),size(frame_1,2));
        image_1 = rgb2gray(frame_1);
        image_2 = rgb2gray(frame_2);
        gauss_filter = fspecial('gaussian',[3 3],sigma);
        sum_filter = ones(m,m);
        image_1 = conv2(image_1,gauss_filter,'same');
        image_2 = conv2(image_2,gauss_filter,'same');
        [Fx, Fy] = gradient(double(image_1));
        diff = double(image_2 - image_1);
        %elements in second moment matrix
        M_11 = Fx.^2;
        M_12 = Fx.*Fy;
        M_21 = Fx.*Fy;
        M_22 = Fy.^2;
        %elements in right vector
        A_1 = diff.*Fx;
        A_2 = diff.*Fy;
        M_11 = conv2(M_11,sum_filter,'same');
        M_12 = conv2(M_12,sum_filter,'same');
        M_21 = conv2(M_21,sum_filter,'same');
        M_22 = conv2(M_22,sum_filter,'same');
        A_1 = conv2(A_1,sum_filter,'same');
        A_2 = conv2(A_2,sum_filter,'same');
        for i=(m-1)/2:size(frame_1,1)-(m-1)/2
            for j=(m-1)/2:size(frame_1,2)-(m-1)/2
                %compute Vx and Vy
                M_sub = [M_11(i,j) M_12(i,j);M_21(i,j) M_22(i,j)];
                A_sub = [A_1(i,j);A_2(i,j)];
                if min(eig(M_sub)) == 0
                    continue;
                end
                V = -inv(M_sub)*A_sub;
                Vx(i,j) = V(1,1);
                Vy(i,j) = V(2,1);
            end
        end
    case "LK_iterative"
        % YOUR IMPLEMENTATION GOES HERE
        m = 19;
        sigma = 1; 
        Vx = zeros(size(frame_1,1),size(frame_1,2));
        Vy = zeros(size(frame_1,1),size(frame_1,2));
        image_1 = rgb2gray(frame_1);
        image_2 = rgb2gray(frame_2);
        gauss_filter = fspecial('gaussian',[3 3],sigma);
        sum_filter = ones(m,m);
        image_1 = conv2(image_1,gauss_filter,'same');
        image_2 = conv2(image_2,gauss_filter,'same');
        t=1;
        image_2_original = image_2;
        while true
            if t>3
                break; 
            end
            disp(t);
            t = t+1;
            %update image_1
            for i=1:size(frame_1,1)
                for j=1:size(frame_1,2)
                    if i+round(Vx(i,j))>size(frame_1,1)
                        x_index = size(frame_1,1);
                    elseif i+round(Vx(i,j))<1
                        x_index = 1;
                    else
                        x_index = i+round(Vx(i,j));
                    end
                    if j+round(Vy(i,j))>size(frame_1,2)
                        y_index = size(frame_1,2);
                    elseif j+round(Vy(i,j))<1
                        y_index = 1;
                    else
                        y_index = j+round(Vy(i,j));
                    end
                    image_2(i,j) = image_2_original(x_index,y_index);
                end
            end
            [Fx, Fy] = gradient(double(image_1));
            diff = double(image_2-image_1);
            %elements in second moment matrix
            M_11 = Fx.^2;
            M_12 = Fx.*Fy;
            M_21 = Fx.*Fy;  
            M_22 = Fy.^2;
            M_11 = conv2(M_11,sum_filter,'same');
            M_12 = conv2(M_12,sum_filter,'same');
            M_21 = conv2(M_21,sum_filter,'same');
            M_22 = conv2(M_22,sum_filter,'same');
            %elements in right vector
            A_1 = diff.*Fx;
            A_2 = diff.*Fy;
            A_1 = conv2(A_1,sum_filter,'same');
            A_2 = conv2(A_2,sum_filter,'same');
            V_x = [];
            V_y = [];
            for i=(m-1)/2:size(frame_1,1)-(m-1)/2
                for j=(m-1)/2:size(frame_1,2)-(m-1)/2
                    %compute Vx and Vy
                    M_sub = [M_11(i,j) M_12(i,j);M_21(i,j) M_22(i,j)];
                    A_sub = [A_1(i,j);A_2(i,j)];
                    if min(eig(M_sub))==0
                        continue;
                    end
                    V = -inv(M_sub)*A_sub;
                    Vx(i,j) = V(1,1) + Vx(i,j);
                    Vy(i,j) = V(2,1) + Vy(i,j);
                    V_x = [V_x;V(1,1)];
                    V_y = [V_y;V(2,1)];
                end
            end
            if norm(V_x)<0.01 && norm(V_y)<0.01
                break;
            end
        end
    case "LK_pyramid"
%       YOUR IMPLEMENTATION GOES HERE
        %create gaussian pyramid
        original_image_1 = rgb2gray(frame_1);
        original_image_2 = rgb2gray(frame_2);
        pyramid_1 = cell(3,1);
        pyramid_2 = cell(3,1);
        gauss_filter = fspecial('gaussian',[3 3],1);
        for level=1:3
            if level == 1
                pyramid_1{level} = conv2(original_image_1,gauss_filter,'same');
                pyramid_2{level} = conv2(original_image_2,gauss_filter,'same');
            end
            if level ~= 1
                pyramid_1{level} = imgaussfilt(pyramid_1{level},2^(level-1));
                pyramid_2{level} = imgaussfilt(pyramid_2{level},2^(level-1));
                pyramid_1{level} = impyramid(pyramid_1{level-1},'reduce');
                pyramid_2{level} = impyramid(pyramid_2{level-1},'reduce');
            end
        end
        %initialize parameters
        m = 15;
        sum_filter = ones(m,m);
        Vx = zeros(size(pyramid_2{3},1),size(pyramid_2{3},2));
        Vy = zeros(size(pyramid_2{3},1),size(pyramid_2{3},2));
        Gx = zeros(size(pyramid_2{3},1),size(pyramid_2{3},2));
        Gy = zeros(size(pyramid_2{3},1),size(pyramid_2{3},2));
        for n = 1:3
            level = 4-n;
            t=1;
            %iterating with each level
            while true
                if t>5
                    break; 
                end
                disp("Level"+level+" :iterating "+t+" times");
                t = t+1;
                Vx = zeros(size(pyramid_2{level},1),size(pyramid_2{level},2));
                Vy = zeros(size(pyramid_2{level},1),size(pyramid_2{level},2));
                %update image_2
                image_2 = zeros(size(pyramid_2{level}));
                for i=1:size(pyramid_2{level},1)
                    for j=1:size(pyramid_2{level},2)
                        if i+round(Gx(i,j)+Vx(i,j))>size(pyramid_2{level},1)
                            x_index = size(pyramid_2{level},1);
                        elseif i+round(Gx(i,j)+Vx(i,j))<1
                            x_index = 1;
                        else
                            x_index = i+round(Gx(i,j)+Vx(i,j));
                        end
                        if j+round(Gy(i,j)+Vy(i,j))>size(pyramid_2{level},2)
                            y_index = size(pyramid_2{level},2);
                        elseif j+round(Gy(i,j)+Vy(i,j))<1
                            y_index = 1;
                        else
                            y_index = j+round(Gy(i,j)+Vy(i,j));
                        end
                        image_2(i,j) = pyramid_2{level}(x_index,y_index);
                    end
                end
                [Fx, Fy] = gradient(double(pyramid_1{level}));
                diff = double(image_2-pyramid_1{level});
                %elements in second moment matrix
                M_11 = Fx.^2;
                M_12 = Fx.*Fy;
                M_21 = Fx.*Fy;  
                M_22 = Fy.^2;
                M_11 = conv2(M_11,sum_filter,'same');
                M_12 = conv2(M_12,sum_filter,'same');
                M_21 = conv2(M_21,sum_filter,'same');
                M_22 = conv2(M_22,sum_filter,'same');
                %elements in right vector
                A_1 = diff.*Fx;
                A_2 = diff.*Fy;
                A_1 = conv2(A_1,sum_filter,'same');
                A_2 = conv2(A_2,sum_filter,'same');
                V_x = [];
                V_y = [];
                for i=1:size(pyramid_2{level},1)
                    for j=1:size(pyramid_2{level},2)
                        %compute Vx and Vy
                        M_sub = [M_11(i,j) M_12(i,j);M_21(i,j) M_22(i,j)];
                        A_sub = [A_1(i,j);A_2(i,j)];
                        if min(eig(M_sub))==0
                            continue;
                        end
                        V = -inv(M_sub)*A_sub;
                        Vx(i,j) = V(1,1) + Vx(i,j);
                        Vy(i,j) = V(2,1) + Vy(i,j);
                        V_x = [V_x;V(1,1)];
                        V_y = [V_y;V(2,1)];
                    end
                end
                if norm(V_x)<0.1 && norm(V_y)<0.1
                    break;
                end
            end
            if level~=1
                Vx = imresize(Vx,size(pyramid_2{level-1}));
                Vy = imresize(Vy,size(pyramid_2{level-1}));
                Gx = imresize(Gx,size(pyramid_2{level-1}));
                Gy = imresize(Gy,size(pyramid_2{level-1}));
                Gx = Gx + Vx;
                Gy = Gy + Vy;
                Gx = Gx.*2;
                Gy = Gy.*2;
            end

        end
        Vx = Vx+Gx;
        Vy = Vy+Gy;
end
end