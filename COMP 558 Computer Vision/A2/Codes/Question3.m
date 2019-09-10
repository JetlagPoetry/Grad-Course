%Code from Assignment1 Question2
original_image = imread('james.jpg');
original_image = original_image(:,:,3);
filter = fspecial('gaussian',[3 3],0.5);
image_gaussian = conv2(original_image,filter,'same');
threshold = 14;
[Fx, Fy] = gradient(double(image_gaussian));
magnitude = sqrt(Fx.^2+Fy.^2);
for i = 1:size(magnitude,1)
    for j = 1:size(magnitude,2)
        if magnitude(i,j) > threshold
            magnitude(i,j) = 255;
        else
            magnitude(i,j) = 0;
        end
    end
end
orientation=atan2(Fy,Fx);
imwrite(magnitude,strcat('Q3_edgemap.jpg'));

%Find non-zero elements of magnitude and build vectors x,y,theta
[row col] = find(magnitude);
theta = sub2ind(size(orientation),row,col);
%RANSAC-like implementation
T=1000;
max_count = 0;
for t=1:T
    disp(t);
    inlier_count=0;
    index = randi(size(theta,1),1);
    x0 = col(index);
    y0 = row(index);
    slope = tan(theta(index)+pi/2);
    %Current line is (x-x0)*slope+(y-y0)=0
    %In the form of Ax+By+C=0
    a = slope;
    b = 1;
    c = -x0*slope-y0;
    distance_matrix = abs(col*a + row*b +c)/sqrt(a^2+b^2);
    distance_matrix(find(rem(theta - theta(index)+2*pi,2*pi))<0.5) = 1000;
    inlier = find(distance_matrix< 2);
    %If inlier more than previous best, save current model
    if size(inlier,1)>max_count
        max_count = size(inlier,1);
        best_line = [x0 y0 theta(index)];
        best_inlier_set = inlier;
    end
end
disp(size(inlier));
%Create a new picture
r = ones(512,512) * 255;
g = ones(512,512) * 255;
b = ones(512,512) * 255;
r(find(magnitude)) = 0;
g(find(magnitude)) = 0;
b(find(magnitude)) = 0;
r(sub2ind(size(r),row(best_inlier_set),col(best_inlier_set))) = 255;
imshow(cat(3,r,g,b));
