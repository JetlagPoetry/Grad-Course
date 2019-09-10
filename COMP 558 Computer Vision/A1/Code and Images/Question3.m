%read the image, take gray image of it
image = imread('myimage3.jpg');
disp(size(image))
image = rgb2gray(image);
%define an array of simga
sigma = [0.5 1 2 4];
for n = 1:size(sigma,2)
    %conv the pic with my LOG filter
    image_filtered = conv2(image,make2DLOG(sigma(n)),'same');
    zero_crossing = zeros(size(image_filtered,1),size(image_filtered,2))
    %go through every pixel, find if the opposite neighbour have oppoosite
    %signs
    for i = 2:size(image_filtered,1)-1
        for j = 2:size(image_filtered,2)-1
            if image_filtered(i-1,j)>=0 && image_filtered(i+1,j)<0 || image_filtered(i-1,j)<0 && image_filtered(i+1,j)>=0
                zero_crossing(i,j) = 1;
            elseif image_filtered(i,j-1)>=0 && image_filtered(i,j+1)<0 || image_filtered(i,j-1)<0 && image_filtered(i,j+1)>=0
                zero_crossing(i,j) = 1;
            elseif image_filtered(i+1,j-1)>=0 && image_filtered(i-1,j+1)<0 || image_filtered(i+1,j-1)<0 && image_filtered(i-1,j+1)>=0
                zero_crossing(i,j) = 1;
            elseif image_filtered(i-1,j-1)>=0 && image_filtered(i+1,j+1)<0 || image_filtered(i-1,j-1)<0 && image_filtered(i+1,j+1)>=0
                zero_crossing(i,j) = 1;
            end
        end
    end
    imwrite(zero_crossing,strcat(num2str(sigma(n)),'_zerocrossing.jpg'));
end